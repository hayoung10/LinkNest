package com.linknest.backend.bookmark;

import com.linknest.backend.common.dto.IdCount;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    // -------------------- Bookmarks (Collection) --------------------
    @Query("select distinct b from Bookmark b " +
            "   left join b.bookmarkTags bt " +
            "   left join bt.tag t " +
            "where b.user.id = :userId " +
            "   and b.collection.id = :collectionId " +
            "   and b.deletedAt is null " +
            "   and (:pattern is null " +
            "       or lower(coalesce(b.title, '')) like :pattern escape '\\' " +
            "       or lower(coalesce(b.url, '')) like :pattern escape '\\' " +
            "       or lower(coalesce(t.name, '')) like :pattern escape '\\')")
    Slice<Bookmark> findAllByCollectionWithSearch(@Param("userId") Long userId, @Param("collectionId") Long collectionId,
                                            @Param("pattern") String pattern, Pageable pageable);

    @Query("select distinct b from Bookmark b " +
            "   left join b.bookmarkTags bt " +
            "   left join bt.tag t " +
            "where b.user.id = :userId " +
            "   and b.collection.id = :collectionId " +
            "   and b.deletedAt is null " +
            "   and (:pattern is null " +
            "       or lower(coalesce(b.title, '')) like :pattern escape '\\' " +
            "       or lower(coalesce(b.url, '')) like :pattern escape '\\' " +
            "       or lower(coalesce(t.name, '')) like :pattern escape '\\') " +
            "order by " +
            "   case when b.title is null or b.title = '' then 1 else 0 end, " +
            "   b.title asc, b.createdAt desc, b.id desc")
    Slice<Bookmark> findAllByCollectionWithSearchSortedByTitle(@Param("userId") Long userId, @Param("collectionId") Long collectionId,
                                                               @Param("pattern") String pattern, Pageable pageable);

    long countByCollectionIdAndDeletedAtIsNull(Long collectionId);

    @Query("select new com.linknest.backend.common.dto.IdCount(b.collection.id, count(b)) " +
            "from Bookmark b " +
            "where b.collection.id in :collectionIds " +
            "   and b.deletedAt is null " +
            "group by b.collection.id")
    List<IdCount> countByCollectionIds(@Param("collectionIds") List<Long> collectionIds);

    // -------------------- Favorite Bookmarks --------------------
    @Query("select distinct b from Bookmark b " +
            "   left join b.bookmarkTags bt " +
            "   left join bt.tag t " +
            "where b.user.id = :userId " +
            "   and b.isFavorite = true " +
            "   and b.deletedAt is null " +
            "   and (:pattern is null " +
            "       or lower(coalesce(b.title, '')) like :pattern escape '\\' " +
            "       or lower(coalesce(b.url, '')) like :pattern escape '\\' " +
            "       or lower(coalesce(t.name, '')) like :pattern escape '\\')")
    Slice<Bookmark> findAllFavoritesWithSearch(@Param("userId") Long userId, @Param("pattern") String pattern, Pageable pageable);

    @Query("select distinct b from Bookmark b " +
            "   left join b.bookmarkTags bt " +
            "   left join bt.tag t " +
            "where b.user.id = :userId " +
            "   and b.isFavorite = true " +
            "   and b.deletedAt is null " +
            "   and (:pattern is null " +
            "       or lower(coalesce(b.title, '')) like :pattern escape '\\' " +
            "       or lower(coalesce(b.url, '')) like :pattern escape '\\' " +
            "       or lower(coalesce(t.name, '')) like :pattern escape '\\') " +
            "order by " +
            "   case when b.title is null or b.title = '' then 1 else 0 end, " +
            "   b.title asc, b.createdAt desc, b.id desc")
    Slice<Bookmark> findAllFavoritesWithSearchSortedByTitle(@Param("userId") Long userId, @Param("pattern") String pattern,
                                                            Pageable pageable);

    // -------------------- Bookmark Ownership Validation --------------------
    long countByUserIdAndIdIn(Long userId, List<Long> ids);

    Optional<Bookmark> findByIdAndUserId(Long id, Long userId);

    @Query(value = "select * from bookmarks where id = :id and user_id = :userId", nativeQuery = true)
    Optional<Bookmark> findIncludingDeletedByIdAndUserId(Long id, Long userId);

    // -------------------- Bulk Ops --------------------
    @Modifying
    @Query(value =
            "update bookmarks set deleted_at = now(6) " +
            "   where user_id = :userId and collection_id in (:collectionIds) and deleted_at is null", nativeQuery = true)
    int softDeleteAllByCollectionIds(@Param("userId") Long userId, @Param("collectionIds") List<Long> collectionIds);

    @Query(value = "select distinct bt.tag_id from bookmark_tags bt " +
            "   join bookmarks b on b.id = bt.bookmark_id " +
            "where b.user_id = :userId " +
            "   and b.deleted_at is null " +
            "   and b.collection_id in (:collectionIds)", nativeQuery = true)
    Set<Long> findTagIdsByUserIdAndCollectionIds(@Param("userId") Long userId, @Param("collectionIds") List<Long> collectionIds);

    // -------------------- Trash --------------------
    @Query("select count(b) > 0 from Bookmark b " +
            "   join b.collection c " +
            "where b.user.id = :userId and b.id in :ids " +
            "   and b.deletedAt is not null and c.deletedAt is not null")
    boolean existsDeletedParentCollectionByUserIdAndIds(@Param("userId") Long userId, @Param("ids") List<Long> ids);

    @Modifying
    @Query(value = "update bookmarks set deleted_at = null " +
            "where user_id = :userId and deleted_at is not null and id in (:ids)", nativeQuery = true)
    int restoreDeletedByUserIdAndIdIn(Long userId, List<Long> ids);

    @Modifying
    @Query(value = "update bookmarks set deleted_at = null " +
            "where user_id = :userId and deleted_at is not null and collection_id in (:collectionIds)", nativeQuery = true)
    int restoreDeletedByUserIdAndCollectionIds(@Param("userId") Long userId, @Param("collectionIds") List<Long> collectionIds);

    @Modifying
    @Query(value = "update bookmarks b " +
            "   left join collections c on c.id = b.collection_id and c.user_id = b.user_id " +
            "   set b.collection_id = :defaultId " +
            "where b.user_id = :userId " +
            "   and b.deleted_at is not null " +
            "   and b.id in (:ids) " +
            "   and (c.id is null or c.deleted_at is not null)", nativeQuery = true)
    int moveDeletedToDefaultIfParentDeleted(@Param("userId") Long userId, @Param("ids") List<Long> ids,
                                            @Param("defaultId") Long defaultId);

    @Modifying
    @Query(value = "delete from bookmarks where user_id = :userId and deleted_at is not null", nativeQuery = true)
    int deleteAllDeletedByUserId(@Param("userId") Long userId);

    @Modifying
    @Query(value = "delete from bookmarks where user_id = :userId and deleted_at is not null and id = :id", nativeQuery = true)
    int deleteDeletedByUserIdAndId(@Param("userId") Long userId, @Param("id") Long id);

    @Modifying
    @Query(value = "delete from bookmarks " +
            "where user_id = :userId " +
            "   and deleted_at is not null " +
            "   and id in (:ids)", nativeQuery = true)
    int deleteDeletedByUserIdAndIdIn(@Param("userId") Long userId, @Param("ids") List<Long> ids);

    @Query(value = "select distinct bt.tag_id from bookmark_tags bt " +
            "   join bookmarks b on b.id = bt.bookmark_id " +
            "where b.user_id = :userId and b.deleted_at is not null", nativeQuery = true)
    Set<Long> findTagIdsByUserIdAndDeletedBookmarks(@Param("userId") Long userId);

    @Query(value = "select distinct bt.tag_id from bookmark_tags bt " +
            "   join bookmarks b on b.id = bt.bookmark_id " +
            "where b.user_id = :userId and b.id in (:bookmarkIds)", nativeQuery = true)
    Set<Long> findTagIdsByUserIdAndBookmarkIds(@Param("userId") Long userId, @Param("bookmarkIds") List<Long> bookmarkIds);

    @Query(value = "select distinct bt.tag_id from bookmark_tags bt " +
            "   join bookmarks b on b.id = bt.bookmark_id " +
            "where b.user_id = :userId" +
            "   and b.deleted_at is not null " +
            "   and b.collection_id in (:collectionIds)", nativeQuery = true)
    Set<Long> findTagIdsByUserIdAndDeletedBookmarksInCollectionIds(@Param("userId") Long userId, @Param("collectionIds") List<Long> collectionIds);

    // -------------------- Trash (Purge) --------------------
    @Query(value = "select distinct bt.tag_id from bookmark_tags bt " +
            "   join bookmarks b on b.id = bt.bookmark_id " +
            "where b.deleted_at is not null and b.collection_id in (:collectionIds)", nativeQuery = true)
    Set<Long> findTagIdsByDeletedBookmarksInCollectionIds(@Param("collectionIds") List<Long> collectionIds);

    @Query(value = "select b.id from bookmarks b " +
            "   left join collections c on c.id = b.collection_id " +
            "where b.deleted_at is not null " +
            "   and b.deleted_at < :cutoff " +
            "   and (c.id is null or c.deleted_at is null) " +
            "order by b.deleted_at asc, b.id asc " +
            "limit :batchSize", nativeQuery = true)
    List<Long> findExpiredDeletedBookmarkIds(@Param("cutoff") Instant cutoff, @Param("batchSize") int batchSize);

    @Modifying
    @Query(value = "delete from bookmarks where id in (:ids) and deleted_at is not null", nativeQuery = true)
    int deleteDeletedByIdIn(@Param("ids") List<Long> ids);

    @Query(value = "select distinct bt.tag_id from bookmark_tags bt " +
            "where bt.bookmark_id in (:bookmarkIds)", nativeQuery = true)
    Set<Long> findTagIdsByDeletedBookmarkIds(@Param("bookmarkIds") List<Long> bookmarkIds);

    // -------------------- AutoImage --------------------
    @Modifying
    @Query("update Bookmark b " +
            "   set b.autoImageUrl = :autoImageUrl, " +
            "       b.autoImageStatus = com.linknest.backend.bookmark.Bookmark$AutoImageStatus.SUCCESS " +
            "where b.id = :bookmarkId " +
            "   and b.user.id = :userId " +
            "   and b.imageMode = com.linknest.backend.bookmark.Bookmark$ImageMode.AUTO " +
            "   and b.url = :url " +
            "   and (b.autoImageUrl is null or b.autoImageUrl = '')")
    int updateAutoImageSuccessIfAutoMode(@Param("userId") Long userId, @Param("bookmarkId") Long bookmarkId,
                                     @Param("url") String url, @Param("autoImageUrl") String autoImageUrl);

    @Modifying
    @Query("update Bookmark b " +
            "   set b.autoImageUrl = null, " +
            "       b.autoImageStatus = com.linknest.backend.bookmark.Bookmark$AutoImageStatus.FAILED " +
            "where b.id = :bookmarkId " +
            "   and b.user.id = :userId " +
            "   and b.imageMode = com.linknest.backend.bookmark.Bookmark$ImageMode.AUTO " +
            "   and b.url = :url " +
            "   and (b.autoImageUrl is null or b.autoImageUrl = '')")
    int updateAutoImageFailedIfAutoMode(@Param("userId") Long userId, @Param("bookmarkId") Long bookmarkId,
                                     @Param("url") String url);
}
