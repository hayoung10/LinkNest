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
            "       or lower(coalesce(t.name, '')) like :pattern escape '\\') " +
            "order by b.createdAt desc, b.id desc")
    Slice<Bookmark> findAllByCollectionWithSearchOrderByCreatedAtDescIdDesc(@Param("userId") Long userId, @Param("collectionId") Long collectionId,
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
            "order by b.createdAt asc, b.id asc")
    Slice<Bookmark> findAllByCollectionWithSearchOrderByCreatedAtAscIdAsc(@Param("userId") Long userId, @Param("collectionId") Long collectionId,
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
            "   case when b.title is null or b.title = '' then 1 else 0 end, b.title asc, b.createdAt desc, b.id desc")
    Slice<Bookmark> findAllByCollectionWithSearchSortedByTitle(@Param("userId") Long userId, @Param("collectionId") Long collectionId,
                                                               @Param("pattern") String pattern, Pageable pageable);

    long countByCollectionId(Long collectionId);

    @Query("select new com.linknest.backend.common.dto.IdCount(b.collection.id, count(b)) " +
            "from Bookmark b " +
            "where b.collection.id in :collectionIds " +
            "group by b.collection.id")
    List<IdCount> countByCollectionIds(@Param("collectionIds") List<Long> collectionIds);

    // -------------------- Favorite Bookmarks --------------------
    Slice<Bookmark> findAllByUserIdAndIsFavoriteTrueOrderByCreatedAtDescIdDesc(Long userId, Pageable pageable);

    Slice<Bookmark> findAllByUserIdAndIsFavoriteTrueOrderByCreatedAtAscIdAsc(Long userId, Pageable pageable);

    @Query(value = "select b from Bookmark b " +
            "where b.user.id = :userId and b.isFavorite = true " +
            "order by " +
            "   case when b.title is null or b.title = '' then 1 else 0 end, b.title asc, b.createdAt desc, b.id desc")
    Slice<Bookmark> findAllFavoritesSortedByTitle(@Param("userId") Long userId, Pageable pageable);

    // -------------------- Bookmark Ownership Validation --------------------
    long countByUserIdAndIdIn(Long userId, List<Long> ids);
}
