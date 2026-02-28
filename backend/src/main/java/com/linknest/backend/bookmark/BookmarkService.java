package com.linknest.backend.bookmark;

import com.linknest.backend.bookmark.Bookmark.ImageMode;
import com.linknest.backend.bookmark.dto.BookmarkCreateReq;
import com.linknest.backend.bookmark.dto.BookmarkRes;
import com.linknest.backend.bookmark.dto.BookmarkUpdateReq;
import com.linknest.backend.bookmark.event.BookmarkAutoImageRequestedEvent;
import com.linknest.backend.collection.Collection;
import com.linknest.backend.collection.CollectionRepository;
import com.linknest.backend.collection.CollectionService;
import com.linknest.backend.common.dto.SliceResponse;
import com.linknest.backend.common.exception.BusinessException;
import com.linknest.backend.common.exception.ErrorCode;
import com.linknest.backend.storage.Storage;
import com.linknest.backend.tag.Tag;
import com.linknest.backend.tag.TagService;
import com.linknest.backend.user.UserRepository;
import com.linknest.backend.userpreferences.UserPreferencesService;
import com.linknest.backend.userpreferences.domain.DefaultBookmarkSort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkService {
    private final Clock clock;

    private final BookmarkRepository bookmarkRepository;
    private final CollectionRepository collectionRepository;
    private final UserRepository userRepository;

    private final UserPreferencesService userPreferencesService;
    private final TagService tagService;
    private final CollectionService collectionService;

    private final BookmarkMapper mapper;
    private final Storage storage;
    private final ApplicationEventPublisher eventPublisher;

    // ---------- 생성 ----------
    @Transactional
    public BookmarkRes create(Long userId, BookmarkCreateReq req) {
        Collection collection = requireOwnedCollection(userId, req.collectionId());

        Bookmark bookmark = mapper.toEntity(req);
        bookmark.setUser(userRepository.getReferenceById(userId));
        bookmark.setCollection(collection);

        ImageMode mode = (bookmark.getImageMode() != null) ? bookmark.getImageMode() : ImageMode.AUTO;
        bookmark.setImageMode(mode);

        if(mode == ImageMode.CUSTOM) throw new BusinessException(ErrorCode.INVALID_IMAGE_MODE);
        bookmark.setCustomImageUrl(null);

        bookmark.setAutoImageUrl(null);

        Bookmark saved = bookmarkRepository.save(bookmark);
        updateBookmarkTags(saved, req.tags());

        // 비동기 auto image 요청
        if(mode == ImageMode.AUTO) {
            eventPublisher.publishEvent(new BookmarkAutoImageRequestedEvent(userId, saved.getId(), saved.getUrl()));
        }

        return mapper.toRes(saved);
    }

    // ---------- 조회 ----------
    public BookmarkRes get(Long userId, Long id) {
        return mapper.toRes(requireOwnedBookmark(userId, id));
    }

    // ---------- 수정 ----------
    @Transactional
    public BookmarkRes update(Long userId, Long id, BookmarkUpdateReq req) {
        Bookmark bookmark = requireOwnedBookmark(userId, id);

        final String beforeUrl = bookmark.getUrl();
        final ImageMode beforeMode = bookmark.getImageMode();

        mapper.updateFromDto(req, bookmark);

        if(bookmark.getUrl() == null || bookmark.getUrl().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_BOOKMARK_URL);
        }

        boolean urlChanged = !Objects.equals(beforeUrl, bookmark.getUrl());

        ImageMode mode = bookmark.getImageMode();
        if(mode == null) mode = ImageMode.NONE;

        applyImageMode(bookmark, mode, urlChanged);

        boolean switchedToAuto = beforeMode != ImageMode.AUTO && mode == ImageMode.AUTO;
        boolean autoUrlChanged = mode == ImageMode.AUTO && urlChanged;

        if(switchedToAuto || autoUrlChanged) {
            eventPublisher.publishEvent(new BookmarkAutoImageRequestedEvent(userId, bookmark.getId(), bookmark.getUrl()));
        }

        updateBookmarkTags(bookmark, req.tags());

        return mapper.toRes(bookmark);
    }

    // ---------- 삭제 ----------
    @Transactional
    public void delete(Long userId, Long id) {
        Bookmark bookmark = requireOwnedBookmark(userId, id);

        Set<Long> tagIds = bookmark.getBookmarkTags().stream()
                .map(BookmarkTag::getTag)
                .filter(Objects::nonNull)
                .map(Tag::getId)
                .collect(Collectors.toSet());

        bookmarkRepository.delete(bookmark);

        // 태그 제거
        if(!tagIds.isEmpty()) {
            Instant now = Instant.now(clock);
            tagService.onTagsDetached(tagIds, now);
        }
    }

    // ---------- 북마크 목록 조회 ----------
    public SliceResponse<BookmarkRes> listByCollection(Long userId, Long collectionId, String q, int page, int size) {
        requireOwnedCollection(userId, collectionId);

        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 100);

        String pattern = toLikePattern(q);
        DefaultBookmarkSort sort = userPreferencesService.getDefaultBookmarkSort(userId);

        Slice<Bookmark> result = switch(sort) {
            case NEWEST -> {
                Pageable pageable = PageRequest.of(
                        safePage, safeSize,
                        Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))
                );
                yield bookmarkRepository
                        .findAllByCollectionWithSearch(userId, collectionId, pattern, pageable);
            }
            case OLDEST -> {
                Pageable pageable = PageRequest.of(
                        safePage, safeSize,
                        Sort.by(Sort.Order.asc("createdAt"), Sort.Order.asc("id"))
                );
                yield bookmarkRepository
                        .findAllByCollectionWithSearch(userId, collectionId, pattern, pageable);
            }
            case TITLE -> {
                Pageable pageable = PageRequest.of(safePage, safeSize);
                yield bookmarkRepository
                        .findAllByCollectionWithSearchSortedByTitle(userId, collectionId, pattern, pageable);
            }
        };

        return SliceResponse.of(result.map(mapper::toRes));
    }

    // ---------- 이동 (컬렉션 변경) ----------
    @Transactional
    public void move(Long userId, Long id, Long targetCollectionId) {
        Bookmark bookmark = requireOwnedBookmark(userId, id);
        Collection collection = requireOwnedCollection(userId, targetCollectionId);

        // 같은 컬렉션으로 이동인 경우 무시
        if(!bookmark.getCollection().getId().equals(collection.getId()))
            bookmark.setCollection(collection);
    }

    // ---------- 북마크 커버 업로드 ----------
    @Transactional
    public BookmarkRes uploadCover(Long userId, Long id, MultipartFile coverImage) {
        if(coverImage == null || coverImage.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_EMPTY);
        }

        Bookmark bookmark = requireOwnedBookmark(userId, id);

        // 기본 커버 삭제
        String oldImgUrl = bookmark.getCustomImageUrl();
        if(oldImgUrl != null && !oldImgUrl.isBlank()) {
            try {
                storage.delete(oldImgUrl);
            } catch (Exception e) {
                log.warn("Bookmark cover upload: failed to delete old cover. userId={}, bookmarkId={}, url={}, reason={}",
                        userId, id, oldImgUrl, e.getMessage(), e);
            }
        }

        // 새 커버 업로드
        String newImgUrl = storage.upload("bookmark-covers", coverImage);
        bookmark.setCustomImageUrl(newImgUrl);
        bookmark.setImageMode(ImageMode.CUSTOM);

        return mapper.toRes(bookmark);
    }

    // ---------- 북마크 커버 삭제 ----------
    @Transactional
    public BookmarkRes removeCover(Long userId, Long id) {
        Bookmark bookmark = requireOwnedBookmark(userId, id);

        String oldImgUrl = bookmark.getCustomImageUrl();
        if(oldImgUrl != null && !oldImgUrl.isBlank()) {
            deleteStoredCover(id, oldImgUrl);
        }

        applyImageMode(bookmark, ImageMode.AUTO, false);

        eventPublisher.publishEvent(new BookmarkAutoImageRequestedEvent(userId, bookmark.getId(), bookmark.getUrl()));

        return mapper.toRes(bookmark);
    }

    // ---------- imageMode 수정 ----------
    @Transactional
    public BookmarkRes updateImageMode(Long userId, Long id, ImageMode imageMode) {
        Bookmark bookmark = requireOwnedBookmark(userId, id);

        if(imageMode == null || imageMode == ImageMode.CUSTOM) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_MODE);
        }

        ImageMode beforeMode = bookmark.getImageMode();

        applyImageMode(bookmark, imageMode, false);

        if(beforeMode != ImageMode.AUTO && imageMode == ImageMode.AUTO) {
            eventPublisher.publishEvent(new BookmarkAutoImageRequestedEvent(userId, bookmark.getId(), bookmark.getUrl()));
        }

        return mapper.toRes(bookmark);
    }

    // ---------- 즐겨찾기(isFavorite) 수정 ----------
    @Transactional
    public BookmarkRes updateFavorite(Long userId, Long id, boolean isFavorite) {
        Bookmark bookmark = requireOwnedBookmark(userId, id);
        bookmark.setFavorite(isFavorite);
        return mapper.toRes(bookmark);
    }

    // ---------- 즐겨찾기 목록 조회 ----------
    public SliceResponse<BookmarkRes> listByFavorites(Long userId, String q, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 100);

        String pattern = toLikePattern(q);

        DefaultBookmarkSort sort = userPreferencesService.getDefaultBookmarkSort(userId);

        Slice<Bookmark> result = switch(sort) {
            case NEWEST -> {
                Pageable pageable = PageRequest.of(
                        safePage, safeSize,
                        Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))
                );
                yield bookmarkRepository.findAllFavoritesWithSearch(userId, pattern, pageable);
            }
            case OLDEST -> {
                Pageable pageable = PageRequest.of(
                        safePage, safeSize,
                        Sort.by(Sort.Order.asc("createdAt"), Sort.Order.asc("id"))
                );
                yield bookmarkRepository.findAllFavoritesWithSearch(userId, pattern, pageable);
            }
            case TITLE -> {
                Pageable pageable = PageRequest.of(safePage, safeSize);
                yield bookmarkRepository.findAllFavoritesWithSearchSortedByTitle(userId, pattern, pageable);
            }
        };

        return SliceResponse.of(result.map(mapper::toRes));
    }

    // ==========================================================
    // Trash
    // ==========================================================
    @Transactional
    public void restoreFromTrash(Long userId, Long id) {
        Bookmark b = requiredOwnedBookmarkIncludingDeleted(userId, id);

        if(!b.isDeleted()) return;

        // 부모 컬렉션이 없으면 디폴트 컬렉션으로 이동
        Collection defaultC = collectionService.getOrCreateDefaultCollection(userId);
        bookmarkRepository.moveDeletedToDefaultIfParentDeleted(userId, List.of(id), defaultC.getId());
        bookmarkRepository.restoreDeletedByUserIdAndIdIn(userId, List.of(id));

        // 태그 orphanedAt 해제
        Set<Long> tagIds = bookmarkRepository.findTagIdsByUserIdAndBookmarkIds(userId, List.of(id));
        if(!tagIds.isEmpty()) {
            tagService.onTagsAttached(tagIds);
        }
    }

    @Transactional
    public void restoreFromTrashBulk(Long userId, List<Long> ids) {
        if(ids == null || ids.isEmpty()) return;

        Collection defaultC = collectionService.getOrCreateDefaultCollection(userId);

        bookmarkRepository.moveDeletedToDefaultIfParentDeleted(userId, ids, defaultC.getId());
        bookmarkRepository.restoreDeletedByUserIdAndIdIn(userId, ids);

        // 태그 orphanedAt 해제
        Set<Long> tagIds = bookmarkRepository.findTagIdsByUserIdAndBookmarkIds(userId, ids);
        tagService.onTagsAttached(tagIds);
    }

    @Transactional
    public void deleteFromTrash(Long userId, Long id) {
        Bookmark b = requiredOwnedBookmarkIncludingDeleted(userId, id);
        if(!b.isDeleted()) throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);

        Set<Long> tagIds = b.getBookmarkTags().stream()
                .map(BookmarkTag::getTag)
                .filter(Objects::nonNull)
                .map(Tag::getId)
                .collect(Collectors.toSet());

        int deleted = bookmarkRepository.deleteDeletedByUserIdAndId(userId, id);

        if(deleted > 0 && !tagIds.isEmpty()) {
            tagService.onTagsDetached(tagIds, Instant.now(clock));
        }
    }

    @Transactional
    public void deleteAllFromTrash(Long userId) {
        Set<Long> tagIds = bookmarkRepository.findTagIdsByUserIdAndDeletedBookmarks(userId);

        bookmarkRepository.deleteAllDeletedByUserId(userId);

        // orphanedAt 갱신
        if(!tagIds.isEmpty()) {
            Instant now = Instant.now(clock);
            tagService.onTagsDetached(tagIds, now);
        }
    }

    @Transactional
    public void deleteFromTrashBulk(Long userId, List<Long> ids) {
        if(ids == null || ids.isEmpty()) return;

        Set<Long> tagIds = bookmarkRepository.findTagIdsByUserIdAndBookmarkIds(userId, ids);

        bookmarkRepository.deleteDeletedByUserIdAndIdIn(userId, ids);

        // orphanedAt 갱신
        tagService.onTagsDetached(tagIds, Instant.now(clock));
    }

    // ==========================================================
    // 내부 유틸
    // ==========================================================
    private Bookmark requireOwnedBookmark(Long userId, Long id) {
        return bookmarkRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOKMARK_NOT_FOUND));
    }

    private Bookmark requiredOwnedBookmarkIncludingDeleted(Long userId, Long id) {
        return bookmarkRepository.findIncludingDeletedByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOKMARK_NOT_FOUND));
    }

    private Collection requireOwnedCollection(Long userId, Long collectionId) {
        Collection collection =  collectionRepository.findById(collectionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COLLECTION_NOT_FOUND));
        if(!collection.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        return collection;
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

    private void applyImageMode(Bookmark bookmark, ImageMode mode, boolean urlChanged) {
        if(mode == null) throw new BusinessException(ErrorCode.INVALID_IMAGE_MODE);
        if(mode == ImageMode.CUSTOM) return;

        final String beforeCustomUrl = bookmark.getCustomImageUrl();
        if(beforeCustomUrl != null && !beforeCustomUrl.isBlank()) {
            deleteStoredCover(bookmark.getId(), beforeCustomUrl);
        }

        bookmark.setImageMode(mode);
        bookmark.setCustomImageUrl(null);

        if(mode == ImageMode.AUTO) {
            bookmark.setAutoImageUrl(null);
        }
    }

    private void deleteStoredCover(Long id, String url) {
        if(url == null || url.isBlank()) return;

        try {
            storage.delete(url);
        } catch (Exception e) {
            log.warn("Bookmark cover delete: failed to delete old cover. bookmarkId={}, url={}, reason={}",
                    id, url, e.getMessage(), e);
        }
    }

    private void updateBookmarkTags(Bookmark bookmark, List<String> tagNames) {
        if(tagNames == null) return;

        Long userId = bookmark.getUser().getId();
        Instant now = Instant.now(clock);

        // 기존 태그 조회
        Set<Long> existingTagIds = bookmark.getBookmarkTags().stream()
                .map(BookmarkTag::getTag)
                .filter(Objects::nonNull)
                .map(Tag::getId)
                .collect(Collectors.toSet());

        Set<Tag> tags = tagService.getOrCreateByName(userId, tagNames);

        // 요청이 빈 태그면, 전부 제거
        if(tags.isEmpty()) {
            bookmark.getBookmarkTags().clear();
            tagService.onTagsDetached(existingTagIds, now);
            return;
        }

        // 목표 tagId 집합
        Set<Long> targetTagIds = tags.stream().map(Tag::getId).collect(Collectors.toSet());

        // detached / attached
        Set<Long> detachedTagIds = new HashSet<>(existingTagIds);
        detachedTagIds.removeAll(targetTagIds);

        Set<Long> attachedTagIds = new HashSet<>(targetTagIds);
        attachedTagIds.removeAll(existingTagIds);

        // 제거: 목표 집합에 없는 관계 제거
        bookmark.getBookmarkTags().removeIf(bt -> {
            Tag t = bt.getTag();
            if(t == null) return false;
            return !targetTagIds.contains(bt.getTag().getId());
        });

        // 추가: 없는 관계만 추가
        Map<Long, Tag> byId = tags.stream().collect(Collectors.toMap(Tag::getId, Function.identity()));
        for(Long tagId : attachedTagIds) {
            bookmark.getBookmarkTags().add(BookmarkTag.create(bookmark, byId.get(tagId)));
        }

        // orphanedAt 처리
        if(!detachedTagIds.isEmpty()) {
            tagService.onTagsDetached(detachedTagIds, now);
        }
        if(!attachedTagIds.isEmpty()) {
            tagService.onTagsAttached(attachedTagIds);
        }
    }
}
