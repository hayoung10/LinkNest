package com.linknest.backend.bookmark;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookmarkTagRepository extends JpaRepository<BookmarkTag, BookmarkTagId> {
    @Query("select count(distinct b.id) " +
            "from BookmarkTag bt " +
            "   join bt.bookmark b " +
            "where b.user.id = :userId and bt.tag.id = :tagId")
    long countDistinctBookmarksByUserIdAndTagId(@Param("userId") Long userId, @Param("tagId") Long tagId);

    @Query("select b.id " +
            "from BookmarkTag bt " +
            "   join bt.bookmark b " +
            "where b.user.id = :userId and bt.tag.id = :tagId")
    List<Long> findAllBookmarkIdsByUserIdAndTagId(@Param("userId") Long userId, @Param("tagId") Long tagId);

    @Query("select case when count(bt) > 0 then true else false end " +
            "from BookmarkTag bt " +
            "   join bt.bookmark b " +
            "where b.user.id = :userId and bt.tag.id = :tagId")
    boolean existsByUserIdAndTagId(@Param("userId") Long userId, @Param("tagId") Long tagId);

    boolean existsByBookmark_IdAndTag_Id(Long bookmarkId, Long tagId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from BookmarkTag bt " +
            "where bt.bookmark.id = :bookmarkId and bt.tag.id = :tagId")
    void deleteByBookmarkIdAndTagId(@Param("bookmarkId") Long bookmarkId, @Param("tagId") Long tagId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update BookmarkTag bt " +
            "set bt.tag.id = :toTagId " +
            "where bt.bookmark.id = :bookmarkId and bt.tag.id = :fromTagId")
    int replaceTagOnBookmark(@Param("bookmarkId") Long bookmarkId, @Param("fromTagId") Long fromTagId, @Param("toTagId") Long toTagId);
}
