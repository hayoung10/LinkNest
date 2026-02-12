package com.linknest.backend.bookmark;

import com.linknest.backend.common.dto.IdCount;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    // -------------------- Bookmarks (Collection) --------------------
    @Query("select distinct b from Bookmark b " +
            "   left join b.bookmarkTags bt " +
            "   left join bt.tag t " +
            "where b.user.id = :userId " +
            "   and b.collection.id = :collectionId " +
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
            "   and (:pattern is null " +
            "       or lower(coalesce(b.title, '')) like :pattern escape '\\' " +
            "       or lower(coalesce(b.url, '')) like :pattern escape '\\' " +
            "       or lower(coalesce(t.name, '')) like :pattern escape '\\') " +
            "order by " +
            "   case when b.title is null or b.title = '' then 1 else 0 end, " +
            "   b.title asc, b.createdAt desc, b.id desc")
    Slice<Bookmark> findAllByCollectionWithSearchSortedByTitle(@Param("userId") Long userId, @Param("collectionId") Long collectionId,
                                                               @Param("pattern") String pattern, Pageable pageable);

    long countByCollectionId(Long collectionId);

    @Query("select new com.linknest.backend.common.dto.IdCount(b.collection.id, count(b)) " +
            "from Bookmark b " +
            "where b.collection.id in :collectionIds " +
            "group by b.collection.id")
    List<IdCount> countByCollectionIds(@Param("collectionIds") List<Long> collectionIds);

    // -------------------- Favorite Bookmarks --------------------
    @Query("select distinct b from Bookmark b " +
            "   left join b.bookmarkTags bt " +
            "   left join bt.tag t " +
            "where b.user.id = :userId " +
            "   and b.isFavorite = true " +
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
}
