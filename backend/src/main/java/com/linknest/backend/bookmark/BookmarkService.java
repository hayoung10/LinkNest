package com.linknest.backend.bookmark;

import com.linknest.backend.bookmark.Bookmark.ImageMode;
import com.linknest.backend.bookmark.dto.BookmarkCreateReq;
import com.linknest.backend.bookmark.dto.BookmarkRes;
import com.linknest.backend.bookmark.dto.BookmarkUpdateReq;
import com.linknest.backend.bookmark.preview.BookmarkPreviewService;
import com.linknest.backend.collection.Collection;
import com.linknest.backend.collection.CollectionRepository;
import com.linknest.backend.common.exception.BusinessException;
import com.linknest.backend.common.exception.ErrorCode;
import com.linknest.backend.storage.Storage;
import com.linknest.backend.tag.Tag;
import com.linknest.backend.tag.TagRepository;
import com.linknest.backend.user.UserRepository;
import com.linknest.backend.userpreferences.UserPreferencesService;
import com.linknest.backend.userpreferences.domain.DefaultBookmarkSort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private static final int MAX_TAGS = 3;

    private final BookmarkRepository bookmarkRepository;
    private final CollectionRepository collectionRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final BookmarkTagRepository bookmarkTagRepository;
    private final UserPreferencesService userPreferencesService;
    private final BookmarkPreviewService previewService;

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
    public List<BookmarkRes> listByCollection(Long userId, Long collectionId) {
        requireOwnedCollection(userId, collectionId);

        DefaultBookmarkSort sort = userPreferencesService.getDefaultBookmarkSort(userId);

        List<Bookmark> list = switch(sort) {
            case NEWEST -> bookmarkRepository
                    .findAllByUserIdAndCollectionIdOrderByCreatedAtDesc(userId, collectionId);
            case OLDEST -> bookmarkRepository
                    .findAllByUserIdAndCollectionIdOrderByCreatedAtAsc(userId, collectionId);
            case TITLE -> bookmarkRepository
                    .findAllSortedByTitle(userId, collectionId);
        };

        return list.stream().map(mapper::toRes).toList();
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
    public List<BookmarkRes> listByFavorites(Long userId) {
        DefaultBookmarkSort sort = userPreferencesService.getDefaultBookmarkSort(userId);

        List<Bookmark> list = switch(sort) {
            case NEWEST -> bookmarkRepository.findAllByUserIdAndIsFavoriteTrueOrderByCreatedAtDesc(userId);
            case OLDEST -> bookmarkRepository.findAllByUserIdAndIsFavoriteTrueOrderByCreatedAtAsc(userId);
            case TITLE -> bookmarkRepository.findAllFavoritesSortedByTitle(userId);
        };

        return list.stream().map(mapper::toRes).toList();
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

        Set<String> names = tagNames.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(t -> !t.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if(names.size() > MAX_TAGS) {
            throw new BusinessException(ErrorCode.TAG_LIMIT_EXCEEDED);
        }

        if(names.isEmpty()) {
            bookmark.getBookmarkTags().clear();
            return;
        }

        Long userId = bookmark.getUser().getId();

        // 기존 태그 조회
        Map<String, Tag> byName = tagRepository.findByUserIdAndNameIn(userId, names).stream()
                .collect(Collectors.toMap(Tag::getName, Function.identity()));

        // 없는 태그는 생성
        List<Tag> toCreate = names.stream()
                .filter(n -> !byName.containsKey(n))
                .map(n -> Tag.builder()
                        .user(bookmark.getUser())
                        .name(n)
                        .build())
                .toList();

        if(!toCreate.isEmpty()) {
            tagRepository.saveAll(toCreate).forEach(t -> byName.put(t.getName(), t));
        }

        // 목표 tagId 집합
        Set<Long> targetTagIds = names.stream()
                .map(n -> byName.get(n).getId())
                .collect(Collectors.toSet());

        Set<Long> existingTagIds = bookmark.getBookmarkTags().stream()
                .map(bt -> bt.getTag().getId())
                .collect(Collectors.toSet());

        // 제거: 목표 집합에 없는 BookmarkTag 제거
        bookmark.getBookmarkTags().removeIf(bt -> !targetTagIds.contains(bt.getTag().getId()));

        // 추가
        for(String name: names) {
            Tag tag = byName.get(name);
            if(existingTagIds.contains(tag.getId())) continue;
            bookmark.getBookmarkTags().add(BookmarkTag.create(bookmark, tag));
        }
    }
}
