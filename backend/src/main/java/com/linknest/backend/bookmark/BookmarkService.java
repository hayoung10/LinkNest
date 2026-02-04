package com.linknest.backend.bookmark;

import com.linknest.backend.bookmark.Bookmark.ImageMode;
import com.linknest.backend.bookmark.dto.BookmarkCreateReq;
import com.linknest.backend.bookmark.dto.BookmarkRes;
import com.linknest.backend.bookmark.dto.BookmarkUpdateReq;
import com.linknest.backend.bookmark.preview.BookmarkPreviewService;
import com.linknest.backend.collection.Collection;
import com.linknest.backend.collection.CollectionRepository;
import com.linknest.backend.common.dto.PageResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkService {
    private final BookmarkRepository bookmarkRepository;
    private final CollectionRepository collectionRepository;
    private final UserRepository userRepository;
    private final UserPreferencesService userPreferencesService;
    private final BookmarkPreviewService previewService;
    private final TagService tagService;

    private final BookmarkMapper mapper;
    private final Storage storage;

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

        if(mode == ImageMode.AUTO) {
            String autoImageUrl = previewService.extractAutoImageUrl(req.url()).orElse(null);
            bookmark.setAutoImageUrl(autoImageUrl);
        } else {
            bookmark.setAutoImageUrl(null);
        }

        Bookmark saved = bookmarkRepository.save(bookmark);
        updateBookmarkTags(saved, req.tags());

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

        mapper.updateFromDto(req, bookmark);

        if(bookmark.getUrl() == null || bookmark.getUrl().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_BOOKMARK_URL);
        }

        ImageMode imageMode = bookmark.getImageMode();
        if(imageMode == null) {
            imageMode = ImageMode.NONE;
        }

        boolean urlChanged = !Objects.equals(beforeUrl, bookmark.getUrl());
        applyImageMode(bookmark, imageMode, urlChanged);

        updateBookmarkTags(bookmark, req.tags());

        return mapper.toRes(bookmark);
    }

    // ---------- 삭제 ----------
    @Transactional
    public void delete(Long userId, Long id) {
        Bookmark bookmark = requireOwnedBookmark(userId, id);
        bookmarkRepository.delete(bookmark);
    }

    // ---------- 북마크 목록 조회 ----------
    public PageResponse<BookmarkRes> listByCollection(Long userId, Long collectionId, int page, int size) {
        requireOwnedCollection(userId, collectionId);

        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize);

        DefaultBookmarkSort sort = userPreferencesService.getDefaultBookmarkSort(userId);

        Page<Bookmark> result = switch(sort) {
            case NEWEST -> bookmarkRepository
                    .findAllByUserIdAndCollectionIdOrderByCreatedAtDesc(userId, collectionId, pageable);
            case OLDEST -> bookmarkRepository
                    .findAllByUserIdAndCollectionIdOrderByCreatedAtAsc(userId, collectionId, pageable);
            case TITLE -> bookmarkRepository
                    .findAllSortedByTitle(userId, collectionId, pageable);
        };

        return PageResponse.of(result.map(mapper::toRes));
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
        bookmark.setCustomImageUrl(null);
        bookmark.setImageMode(ImageMode.AUTO);

        if(bookmark.getAutoImageUrl() == null) {
            bookmark.setAutoImageUrl(previewService.extractAutoImageUrl(bookmark.getUrl()).orElse(null));
        }

        return mapper.toRes(bookmark);
    }

    // ---------- imageMode 수정 ----------
    @Transactional
    public BookmarkRes updateImageMode(Long userId, Long id, ImageMode imageMode) {
        Bookmark bookmark = requireOwnedBookmark(userId, id);

        if(imageMode == null || imageMode == ImageMode.CUSTOM) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_MODE);
        }

        applyImageMode(bookmark, imageMode, false);

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
    public PageResponse<BookmarkRes> listByFavorites(Long userId, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize);

        DefaultBookmarkSort sort = userPreferencesService.getDefaultBookmarkSort(userId);

        Page<Bookmark> result = switch(sort) {
            case NEWEST -> bookmarkRepository.findAllByUserIdAndIsFavoriteTrueOrderByCreatedAtDesc(userId, pageable);
            case OLDEST -> bookmarkRepository.findAllByUserIdAndIsFavoriteTrueOrderByCreatedAtAsc(userId, pageable);
            case TITLE -> bookmarkRepository.findAllFavoritesSortedByTitle(userId, pageable);
        };

        return PageResponse.of(result.map(mapper::toRes));
    }

    // ==========================================================
    // 내부 유틸
    // ==========================================================
    private Bookmark requireOwnedBookmark(Long userId, Long id) {
        Bookmark bookmark = bookmarkRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOKMARK_NOT_FOUND));
        if(!bookmark.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        return bookmark;
    }

    private Collection requireOwnedCollection(Long userId, Long collectionId) {
        Collection collection =  collectionRepository.findById(collectionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COLLECTION_NOT_FOUND));
        if(!collection.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        return collection;
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
            if(bookmark.getAutoImageUrl() == null || urlChanged) {
                bookmark.setAutoImageUrl(previewService.extractAutoImageUrl(bookmark.getUrl()).orElse(null));
            }
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

        Set<Tag> tags = tagService.getOrCreateByName(userId, tagNames);

        if(tags.isEmpty()) {
            bookmark.getBookmarkTags().clear();
            return;
        }

        // 목표 tagId 집합
        Set<Long> targetTagIds = tags.stream().map(Tag::getId).collect(Collectors.toSet());

        // 기존 태그 조회
        Set<Long> existingTagIds = bookmark.getBookmarkTags().stream()
                .map(bt -> bt.getTag().getId())
                .collect(Collectors.toSet());

        // 제거: 목표 집합에 없는 관계 제거
        bookmark.getBookmarkTags().removeIf(bt -> !targetTagIds.contains(bt.getTag().getId()));

        // 추가: 없는 관계만 추가
        Map<Long, Tag> byId = tags.stream().collect(Collectors.toMap(Tag::getId, Function.identity()));
        for(Long tagId : targetTagIds) {
            if(existingTagIds.contains(tagId)) continue;
            bookmark.getBookmarkTags().add(BookmarkTag.create(bookmark, byId.get(tagId)));
        }
    }
}
