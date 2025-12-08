package com.linknest.backend.bookmark;

import com.linknest.backend.bookmark.dto.BookmarkCreateReq;
import com.linknest.backend.bookmark.dto.BookmarkRes;
import com.linknest.backend.bookmark.dto.BookmarkUpdateReq;
import com.linknest.backend.collection.Collection;
import com.linknest.backend.collection.CollectionRepository;
import com.linknest.backend.common.exception.BusinessException;
import com.linknest.backend.common.exception.ErrorCode;
import com.linknest.backend.user.UserRepository;
import com.linknest.backend.userpreferences.UserPreferencesService;
import com.linknest.backend.userpreferences.domain.DefaultBookmarkSort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkService {
    private final BookmarkRepository bookmarkRepository;
    private final CollectionRepository collectionRepository;
    private final UserRepository userRepository;
    private final UserPreferencesService userPreferencesService;

    private final BookmarkMapper mapper;

    // ---------- 생성 ----------
    @Transactional
    public BookmarkRes create(Long userId, BookmarkCreateReq req) {
        Collection collection = requireOwnedCollection(userId, req.collectionId());

        Bookmark bookmark = mapper.toEntity(req);
        bookmark.setUser(userRepository.getReferenceById(userId));
        bookmark.setCollection(collection);

        return mapper.toRes(bookmarkRepository.save(bookmark));
    }

    // ---------- 조회 ----------
    public BookmarkRes get(Long userId, Long id) {
        return mapper.toRes(requireOwnedBookmark(userId, id));
    }

    // ---------- 수정 ----------
    @Transactional
    public BookmarkRes update(Long userId, Long id, BookmarkUpdateReq req) {
        Bookmark bookmark = requireOwnedBookmark(userId, id);
        mapper.updateFromDto(req, bookmark);
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
}
