package com.linknest.backend.tag;

import com.linknest.backend.bookmark.BookmarkRepository;
import com.linknest.backend.bookmark.BookmarkTagRepository;
import com.linknest.backend.common.dto.SliceResponse;
import com.linknest.backend.common.exception.BusinessException;
import com.linknest.backend.common.exception.ErrorCode;
import com.linknest.backend.tag.domain.TagSort;
import com.linknest.backend.tag.dto.*;
import com.linknest.backend.tag.mapper.TagMapper;
import com.linknest.backend.tag.mapper.TaggedBookmarkMapper;
import com.linknest.backend.user.UserRepository;
import com.linknest.backend.userpreferences.UserPreferencesService;
import com.linknest.backend.userpreferences.domain.DefaultBookmarkSort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {
    private static final int MAX_TAGS = 3;

    private final TagRepository tagRepository;
    private final BookmarkTagRepository bookmarkTagRepository;
    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final UserPreferencesService userPreferencesService;

    private final TagMapper tagMapper;
    private final TaggedBookmarkMapper taggedBookmarkMapper;

    @Transactional
    public TagCreateResultRes create(Long userId, TagCreateReq req) {
        String displayName = normalizeName(req.name());
        String nameKey = toNameKey(displayName);

        // 중복 체크 (휴지통 포함)
        Optional<Tag> existingOpt = tagRepository.findIncludingDeletedByUserIdAndNameKey(userId, nameKey);

        if(existingOpt.isPresent()) {
            Tag existing = existingOpt.get();

            if(existing.isDeleted()) {
                existing.restore();
                existing.setName(displayName);
                long count = bookmarkTagRepository.countDistinctBookmarksByUserIdAndTagId(userId, existing.getId());
                return new TagCreateResultRes(tagMapper.toResWithCount(existing, count), true);
            }

            throw new BusinessException(ErrorCode.TAG_NAME_DUPLICATED);
        }

        // Tag 생성
        Tag tag = Tag.builder()
                .user(userRepository.getReferenceById(userId))
                .name(displayName)
                .nameKey(nameKey)
                .build();

        Tag saved = tagRepository.save(tag);
        return new TagCreateResultRes(tagMapper.toResWithCount(saved, 0L), false);
    }

    public SliceResponse<TagRes> getTags(Long userId, String q, TagSort sort, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize);

        String pattern = toLikePattern(q);

        Slice<TagRes> result = switch(sort) {
            case NEWEST -> tagRepository.findAllByUserIdAndNameLikeOrderByCreatedAtDesc(userId, pattern, pageable);
            case OLDEST -> tagRepository.findAllByUserIdAndNameLikeOrderByCreatedAtAsc(userId, pattern, pageable);
            case NAME_ASC -> tagRepository.findAllByUserIdAndNameLikeOrderByNameAsc(userId, pattern, pageable);
            case COUNT_DESC -> tagRepository.findAllByUserIdAndNameLikeOrderByBookmarkCountDesc(userId, pattern, pageable);
        };

        return SliceResponse.of(result);
    }

    @Transactional
    public TagRes rename(Long userId, Long id, TagUpdateReq req) {
        Tag tag = requireOwnedTag(userId, id);

        String displayName = normalizeName(req.name());
        String nameKey = toNameKey(displayName);

        // displayName 혹은 nameKey가 동일한 경우
        if(nameKey.equals(tag.getNameKey())) {
            tag.setName(displayName);
            long count = bookmarkTagRepository.countDistinctBookmarksByUserIdAndTagId(userId, id);
            return tagMapper.toResWithCount(tag, count);
        }

        // 중복 체크 (휴지통 포함)
        tagRepository.findIncludingDeletedByUserIdAndNameKey(userId, nameKey)
                .filter(existingTag -> !existingTag.getId().equals(id))
                .ifPresent(existingTag -> {
                    throw new BusinessException(ErrorCode.TAG_NAME_DUPLICATED);
                });

        tag.setName(displayName);
        tag.setNameKey(nameKey);

        long count = bookmarkTagRepository.countDistinctBookmarksByUserIdAndTagId(userId, id);
        return tagMapper.toResWithCount(tag, count);
    }

    @Transactional
    public void merge(Long userId, Long id, TagMergeReq req) {
        Long targetTagId = req.targetTagId();

        if(id.equals(targetTagId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        requireOwnedTag(userId, id);
        requireOwnedTag(userId, targetTagId);

        List<Long> bookmarkIds = bookmarkTagRepository.findAllBookmarkIdsByUserIdAndTagId(userId, id);
        if(bookmarkIds.isEmpty()) return;

        // 충돌 제거
        List<Long> conflictIds = bookmarkTagRepository.findBookmarkIdsByUserIdAndTagIdAndBookmarkIdIn(userId, targetTagId, bookmarkIds);
        if(conflictIds.isEmpty()) {
            bookmarkTagRepository.deleteByUserIdAndTagIdAndBookmarkIdIn(userId, id, conflictIds);
        }

        Tag toTag = tagRepository.getReferenceById(targetTagId);
        bookmarkTagRepository.replaceTagOnBookmarks(userId, id, toTag, bookmarkIds);
    }

    @Transactional
    public void softDelete(Long userId, Long id) {
        Tag tag = requireOwnedTag(userId, id);
        tag.softDelete();
    }

    @Transactional
    public void hardDelete(Long userId, Long id) {
        requiredOwnedTagIncludingDeleted(userId, id);

        bookmarkTagRepository.deleteByUserIdAndTagId(userId, id);

        tagRepository.hardDeleteByIdAndUserId(id, userId);
    }

    // ==========================================================
    // Tagged Bookmarks
    // ==========================================================

    public SliceResponse<TaggedBookmarkRes> getTaggedBookmarks(Long userId, Long id, int page, int size) {
        requireOwnedTag(userId, id);

        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize);

        DefaultBookmarkSort sort = userPreferencesService.getDefaultBookmarkSort(userId);

        Slice<TaggedBookmarkRow> rows = switch (sort) {
            case NEWEST -> bookmarkTagRepository.findTaggedBookmarksNewest(userId, id, pageable);
            case OLDEST -> bookmarkTagRepository.findTaggedBookmarksOldest(userId, id, pageable);
            case TITLE -> bookmarkTagRepository.findTaggedBookmarksSortedByTitle(userId, id, pageable);
        };

        List<Long> bookmarkIds = rows.getContent().stream()
                .map(TaggedBookmarkRow::id)
                .distinct()
                .toList();

        if(bookmarkIds.isEmpty()) {
            return SliceResponse.of(rows.map(taggedBookmarkMapper::toRes));
        }

        // tags 조회
        List<BookmarkTagNameRow> tagRows = bookmarkTagRepository.findTagNamesByBookmarkIds(bookmarkIds);

        Map<Long, List<String>> tagsByBookmarkId = tagRows.stream()
                .collect(Collectors.groupingBy(
                        BookmarkTagNameRow::bookmarkId,
                        Collectors.mapping(BookmarkTagNameRow::tagName, Collectors.toList())
                ));

        // rows에 tags 주입
        Slice<TaggedBookmarkRow> enriched = rows.map(row ->
                row.withTags(tagsByBookmarkId.getOrDefault(row.id(), List.of()))
        );

        return SliceResponse.of(enriched.map(taggedBookmarkMapper::toRes));
    }

    @Transactional
    public void detachTagFromBookmarks(Long userId, Long id, TagDetachReq req) {
        requireOwnedTag(userId, id);
        requireOwnedBookmarks(userId, req.bookmarkIds());

        bookmarkTagRepository.deleteByUserIdAndTagIdAndBookmarkIdIn(userId, id, req.bookmarkIds());
    }

    @Transactional
    public void replaceTagOnBookmarks(Long userId, Long id, TagReplaceReq req) {
        Long targetTagId = req.targetTagId();

        if(id.equals(targetTagId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        requireOwnedTag(userId, id);
        requireOwnedTag(userId, targetTagId);
        requireOwnedBookmarks(userId, req.bookmarkIds());

        List<Long> bookmarkIds = req.bookmarkIds();

        Set<Long> conflictSet = new HashSet<>(
                bookmarkTagRepository.findBookmarkIdsByUserIdAndTagIdAndBookmarkIdIn(userId, targetTagId, bookmarkIds)
        );

        for(Long bookmarkId : bookmarkIds) {
            if(conflictSet.contains(bookmarkId)) {
                bookmarkTagRepository.deleteByBookmarkIdAndTagId(bookmarkId, id);
            } else {
                bookmarkTagRepository.replaceTagOnBookmark(bookmarkId, id, targetTagId);
            }
        }
    }

    // ==========================================================
    // Summary
    // ==========================================================

    public TagSummaryRes getSummary(Long userId) {
        long totalTags = tagRepository.countByUserIdAndDeletedAtIsNull(userId);
        long totalTaggedBookmarks = bookmarkTagRepository.countDistinctTaggedBookmarks(userId);

        return new TagSummaryRes(totalTags, totalTaggedBookmarks);
    }

    // ==========================================================
    // 공통 유틸 (BookmarkService)
    // ==========================================================

    @Transactional
    public Set<Tag> getOrCreateByName(Long userId, Collection<String> tagNames) {
        if(tagNames == null) return Set.of();

        Map<String, String> keyToDisplay = new LinkedHashMap<>();
        for(String name : tagNames) {
            if(name == null) continue;

            String display = normalizeName(name);
            String nameKey = toNameKey(display);

            keyToDisplay.putIfAbsent(nameKey, display);
        }

        if(keyToDisplay.size() > MAX_TAGS) throw new BusinessException(ErrorCode.TAG_LIMIT_EXCEEDED);
        if(keyToDisplay.isEmpty()) return Set.of();

        Set<String> keys = keyToDisplay.keySet();

        // 기존 태그 조회 (휴지통 포함)
        List<Tag> existing = tagRepository.findAllIncludingDeletedByUserIdAndNameKeyIn(userId, keys);

        Map<String, Tag> byKey = new HashMap<>();
        for(Tag t : existing) {
            if(t.isDeleted()) {
                t.restore();
                t.setName(keyToDisplay.get(t.getNameKey()));
            }
            byKey.put(t.getNameKey(), t);
        }

        // 없는 key 생성
        List<Tag> toCreate = keys.stream()
                .filter(k -> !byKey.containsKey(k))
                .map(k -> Tag.builder()
                        .user(userRepository.getReferenceById(userId))
                        .name(keyToDisplay.get(k))
                        .nameKey(k)
                        .build())
                .toList();

        if(!toCreate.isEmpty()) {
            tagRepository.saveAll(toCreate).forEach(t -> byKey.put(t.getNameKey(), t));
        }

        // 입력 순서를 유지해서 반환
        LinkedHashSet<Tag> result = new LinkedHashSet<>();
        for(String k : keys) result.add(byKey.get(k));
        return result;
    }

    // ==========================================================
    // 내부 유틸
    // ==========================================================

    private String normalizeName(String tagName) {
        if(tagName == null) throw new BusinessException(ErrorCode.TAG_NAME_INVALID);

        // 공백 제거
        String name = tagName.trim().replaceAll("\\s+", "");

        if(name.isBlank()) throw new BusinessException(ErrorCode.TAG_NAME_INVALID);
        if(name.length() > 50) throw new BusinessException(ErrorCode.TAG_NAME_TOO_LONG);

        return name;
    }

    private String toNameKey(String tagName) {
        return normalizeName(tagName).toLowerCase(Locale.ROOT);
    }

    private String toLikePattern(String q) {
        if(q == null) return null;

        String s = q.trim();
        if(s.isBlank()) return null;

        s = s.toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");

        return "%" + s + "%";
    }

    private Tag requireOwnedTag(Long userId, Long id) {
        return tagRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TAG_NOT_FOUND));
    }

    private Tag requiredOwnedTagIncludingDeleted(Long userId, Long id) {
        return tagRepository.findIncludingDeletedByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TAG_NOT_FOUND));
    }

    private void requireOwnedBookmarks(Long userId, List<Long> bookmarkIds) {
        long ownedCount = bookmarkRepository.countByUserIdAndIdIn(userId, bookmarkIds);
        if(ownedCount != bookmarkIds.size()) {
            throw new BusinessException(ErrorCode.BOOKMARK_NOT_FOUND);
        }
    }
}
