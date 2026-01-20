package com.linknest.backend.bookmark;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookmarkTagRepository extends JpaRepository<BookmarkTag, BookmarkTagId> {
    @Query("select count(distinct b.id) " +
            "from BookmarkTag bt " +
            "   join bt.bookmark b " +
            "where b.user.id = :userId and bt.tag.id = :tagId")
    long countDistinctBookmarksByUserIdAndTagId(Long userId, Long tagId);

    @Query("select b.id " +
            "from BookmarkTag bt " +
            "   join bt.bookmark b " +
            "where b.user.id = :userId and bt.tag.id = :tagId")
    List<Long> findAllBookmarkIdsByUserIdAndTagId(Long userId, Long tagId);

    @Query("select case when count(bt) > 0 then true else false end " +
            "from BookmarkTag bt " +
            "   join bt.Bookmark b " +
            "where b.user.id = :userId and bt.tag.id = :id")
    boolean existsByUserIdAndTagId(Long userId, Long id);

    boolean existsByBookmarkIdAndTagId(Long bookmarkId, Long tagId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from BookmarkTag bt " +
            "where bt.bookmark.id = :bookmarkId and bt.tag.id = :tagId")
    void deleteByBookmarkIdAndTagId(Long bookmarkId, Long tagId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update BookmarkTag bt " +
            "set bt.tag.id = :toTagId " +
            "where bt.bookmark.id = :bookmarkId and bt.tag.id = :fromTagId")
    int replaceTagOnBookmark(Long bookmarkId, Long fromTagId, Long toTagId);
}
