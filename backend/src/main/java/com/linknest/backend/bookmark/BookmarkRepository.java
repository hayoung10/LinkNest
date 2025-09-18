package com.linknest.backend.bookmark;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    List<Bookmark> findAllByUserId(Long userId);
    List<Bookmark> findAllByCollectionId(Long collectionId);

}
