package com.linknest.backend.bookmark;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    List<Bookmark> findAllByUserId(Long userId);
    List<Bookmark> indAllByUserIdAndCollectionIdOrderByCreatedAtDesc(Long userId, Long collectionId);

    long countByCollectionId(Long collectionId);
}
