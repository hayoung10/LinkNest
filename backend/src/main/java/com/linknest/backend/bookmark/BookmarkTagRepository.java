package com.linknest.backend.bookmark;

import com.linknest.backend.tag.Tag;
import com.linknest.backend.tag.dto.BookmarkTagNameRow;
import com.linknest.backend.tag.dto.TaggedBookmarkRow;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookmarkTagRepository extends JpaRepository<BookmarkTag, BookmarkTagId> {
    @Query("select count(distinct b.id) " +
            "from BookmarkTag bt " +
            "   join bt.bookmark b " +
            "where b.user.id = :userId " +
            "   and bt.tag.id = :tagId " +
            "   and b.deletedAt is null")
    long countDistinctBookmarksByUserIdAndTagId(@Param("userId") Long userId, @Param("tagId") Long tagId);

    // -------------------- BookmarkIds Query --------------------
    @Query("select b.id " +
            "from BookmarkTag bt " +
            "   join bt.bookmark b " +
            "where b.user.id = :userId " +
            "   and bt.tag.id = :tagId " +
            "   and b.deletedAt is null")
    List<Long> findAllBookmarkIdsByUserIdAndTagId(@Param("userId") Long userId, @Param("tagId") Long tagId);

    @Query("select bt.bookmark.id from BookmarkTag bt " +
            "where bt.bookmark.user.id = :userId " +
            "   and bt.tag.id = :toTagId " +
            "   and bt.bookmark.id in :bookmarkIds " +
            "   and bt.bookmark.deletedAt is null")
    List<Long> findBookmarkIdsByUserIdAndTagIdAndBookmarkIdIn(@Param("userId") Long userId, @Param("toTagId") Long toTagId,
                                                              @Param("bookmarkIds") List<Long> bookmarkIds);

    // -------------------- Single Bookmark Ops --------------------
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from BookmarkTag bt " +
            "where bt.bookmark.id = :bookmarkId and bt.tag.id = :tagId")
    void deleteByBookmarkIdAndTagId(@Param("bookmarkId") Long bookmarkId, @Param("tagId") Long tagId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update BookmarkTag bt " +
            "set bt.tag.id = :toTagId " +
            "where bt.bookmark.id = :bookmarkId and bt.tag.id = :fromTagId")
    int replaceTagOnBookmark(@Param("bookmarkId") Long bookmarkId, @Param("fromTagId") Long fromTagId, @Param("toTagId") Long toTagId);

    // -------------------- Bulk Ops --------------------
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete BookmarkTag bt " +
            "where bt.bookmark.user.id = :userId and bt.tag.id = :tagId")
    int deleteByUserIdAndTagId(@Param("userId") Long userId, @Param("tagId") Long tagId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete BookmarkTag bt " +
            "where bt.bookmark.user.id = :userId and bt.tag.id = :tagId and bt.bookmark.id in :bookmarkIds")
    int deleteByUserIdAndTagIdAndBookmarkIdIn(@Param("userId") Long userId,
                                              @Param("tagId") Long tagId, @Param("bookmarkIds") List<Long> bookmarkIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update BookmarkTag bt " +
            "set bt.tag = :toTag " +
            "where bt.bookmark.user.id = :userId and bt.tag.id = :fromTagId and bt.bookmark.id in :bookmarkIds")
    int replaceTagOnBookmarks(@Param("userId") Long userId, @Param("fromTagId") Long fromTagId,
                              @Param("toTag") Tag toTag, @Param("bookmarkIds") List<Long> bookmarkIds);

    // -------------------- Tagged Bookmarks (Paging) --------------------
    @Query(value = "select new com.linknest.backend.tag.dto.TaggedBookmarkRow(" +
            "   b.id, c.id, c.name, c.emoji, b.url, b.title, b.description, b.emoji, " +
            "   b.autoImageUrl, b.customImageUrl, b.imageMode, b.isFavorite, " +
            "   null, b.createdAt, b.updatedAt" +
            ") from BookmarkTag bt " +
            "   join bt.bookmark b " +
            "   join b.collection c " +
            "where b.user.id = :userId " +
            "   and bt.tag.id = :tagId " +
            "   and b.deletedAt is null " +
            "order by b.createdAt desc, b.id desc")
    Slice<TaggedBookmarkRow> findTaggedBookmarksNewest(@Param("userId") Long userId, @Param("tagId") Long tagId, Pageable pageable);

    @Query(value = "select new com.linknest.backend.tag.dto.TaggedBookmarkRow(" +
            "   b.id, c.id, c.name, c.emoji, b.url, b.title, b.description, b.emoji, " +
            "   b.autoImageUrl, b.customImageUrl, b.imageMode, b.isFavorite, " +
            "   null, b.createdAt, b.updatedAt" +
            ") from BookmarkTag bt " +
            "   join bt.bookmark b " +
            "   join b.collection c " +
            "where b.user.id = :userId " +
            "   and bt.tag.id = :tagId " +
            "   and b.deletedAt is null " +
            "order by b.createdAt asc, b.id asc")
    Slice<TaggedBookmarkRow> findTaggedBookmarksOldest(@Param("userId") Long userId, @Param("tagId") Long tagId, Pageable pageable);

    @Query(value = "select new com.linknest.backend.tag.dto.TaggedBookmarkRow(" +
            "   b.id, c.id, c.name, c.emoji, b.url, b.title, b.description, b.emoji, " +
            "   b.autoImageUrl, b.customImageUrl, b.imageMode, b.isFavorite, " +
            "   null, b.createdAt, b.updatedAt" +
            ") from BookmarkTag bt " +
            "   join bt.bookmark b " +
            "   join b.collection c " +
            "where b.user.id = :userId " +
            "   and bt.tag.id = :tagId " +
            "   and b.deletedAt is null " +
            "order by " +
            "   case when b.title is null or b.title = '' then 1 else 0 end, b.title asc, b.createdAt desc, b.id desc")
    Slice<TaggedBookmarkRow> findTaggedBookmarksSortedByTitle(@Param("userId") Long userId, @Param("tagId") Long tagId, Pageable pageable);

    // -------------------- Tag Names (Enrichment) --------------------
    @Query("select new com.linknest.backend.tag.dto.BookmarkTagNameRow(bt.bookmark.id, t.name) " +
            "from BookmarkTag bt " +
            "   join bt.tag t " +
            "where bt.bookmark.id in :bookmarkIds " +
            "   and t.deletedAt is null")
    List<BookmarkTagNameRow> findTagNamesByBookmarkIds(@Param("bookmarkIds") List<Long> bookmarkIds);

    // -------------------- Tag Summary --------------------
    @Query("select count(distinct bt.bookmark.id) " +
            "from BookmarkTag bt " +
            "where bt.bookmark.user.id = :userId " +
            "   and bt.bookmark.deletedAt is null")
    long countDistinctTaggedBookmarks(@Param("userId") Long userId);
}
