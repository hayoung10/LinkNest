package com.linknest.backend.bookmark;

import com.linknest.backend.bookmark.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkService {
    private final BookmarkRepository repository;
    private final BookmarkMapper mapper;

    @Transactional
    public BookmarkRes create(BookmarkCreateReq req) {
        Bookmark bookmark = repository.save(mapper.toEntity(req));

        return mapper.toRes(bookmark);
    }

    public BookmarkRes get(Long id) {
        Bookmark bookmark = findVerifiedBookmark(id);

        return mapper.toRes(bookmark);
    }

    @Transactional
    public BookmarkRes update(Long id, BookmarkUpdateReq req) {
        Bookmark bookmark = findVerifiedBookmark(id);
        mapper.updateFromDto(req, bookmark);

        return mapper.toRes(bookmark);
    }

    @Transactional
    public void delete(Long id) {
        Bookmark bookmark = findVerifiedBookmark(id);

        repository.delete(bookmark);
    }

    private Bookmark findVerifiedBookmark(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bookmark not found :" + id));
    }
}
