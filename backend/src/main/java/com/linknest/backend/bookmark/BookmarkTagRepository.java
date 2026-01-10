package com.linknest.backend.bookmark;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkTagRepository extends JpaRepository<BookmarkTag, BookmarkTagId> {
    void deleteAllByBookmark(Bookmark bookmark);
}
