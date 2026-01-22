package com.linknest.backend.tag;

import com.linknest.backend.bookmark.BookmarkTagRepository;
import com.linknest.backend.common.dto.PageResponse;
import com.linknest.backend.common.exception.BusinessException;
import com.linknest.backend.common.exception.ErrorCode;
import com.linknest.backend.tag.domain.TagSort;
import com.linknest.backend.tag.dto.TagMergeReq;
import com.linknest.backend.tag.dto.TagUpdateReq;
import com.linknest.backend.tag.dto.TagRes;
import com.linknest.backend.user.User;
import com.linknest.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {
    private static final int MAX_TAGS = 3;

    private final TagRepository tagRepository;
    private final BookmarkTagRepository bookmarkTagRepository;
    private final UserRepository userRepository;
    private final TagMapper tagMapper;

    public String normalizeName(String tagName) {
        if(tagName == null) throw new BusinessException(ErrorCode.TAG_NAME_INVALID);

        // 공백 제거
        String name = tagName.trim().replaceAll("\\s+", "");

        if(name.isBlank()) throw new BusinessException(ErrorCode.TAG_NAME_INVALID);
        if(name.length() > 50) throw new BusinessException(ErrorCode.TAG_NAME_TOO_LONG);

        return name;
    }

    public String toNameKey(String tagName) {
        return normalizeName(tagName).toLowerCase(Locale.ROOT);
    }

    public PageResponse<TagRes> getTags(Long userId, String q, TagSort sort, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<TagRes> result = switch(sort) {
            case NEWEST -> tagRepository.findAllByUserIdAndNameLikeOrderByCreatedAtDesc(userId, q, pageable);
            case OLDEST -> tagRepository.findAllByUserIdAndNameLikeOrderByCreatedAtAsc(userId, q, pageable);
            case NAME_ASC -> tagRepository.findAllByUserIdAndNameLikeOrderByNameAsc(userId, q, pageable);
            case NAME_DESC -> tagRepository.findAllByUserIdAndNameLikeOrderByNameDesc(userId, q, pageable);
            case COUNT_DESC -> tagRepository.findAllByUserIdAndNameLikeOrderByBookmarkCountDesc(userId, q, pageable);
            case COUNT_ASC -> tagRepository.findAllByUserIdAndNameLikeOrderByBookmarkCountAsc(userId, q, pageable);
        };

        return PageResponse.of(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
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

        // 중복 체크
        tagRepository.findByUserIdAndNameKey(userId, nameKey)
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

        for(Long bookmarkId : bookmarkIds) {
            // 이미 targetId로 설정되어 있는 경우
            if(bookmarkTagRepository.existsByBookmark_IdAndTag_Id(bookmarkId, targetTagId)) {
                bookmarkTagRepository.deleteByBookmarkIdAndTagId(bookmarkId, id);
                continue;
            }
            // 없으면 id -> targetTagId 치환
            bookmarkTagRepository.replaceTagOnBookmark(bookmarkId, id, targetTagId);
        }
    }

    @Transactional
    public void delete(Long userId, Long id) {
        requireOwnedTag(userId, id);

        if(bookmarkTagRepository.existsByUserIdAndTagId(userId, id)) {
            throw new BusinessException(ErrorCode.TAG_IN_USE);
        }

        tagRepository.deleteById(id);
    }

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

        // 기존 태그 조회
        List<Tag> existing = tagRepository.findByUserIdAndNameKeyIn(userId, keys);
        Map<String, Tag> byKey = existing.stream()
                .collect(Collectors.toMap(Tag::getNameKey, Function.identity()));

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

    private Tag requireOwnedTag(Long userId, Long id) {
        return tagRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TAG_NOT_FOUND));
    }
}
