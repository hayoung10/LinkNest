package com.linknest.backend.bookmark;

import com.linknest.backend.common.dto.IdCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    List<Bookmark> findAllByUserIdAndCollectionIdOrderByCreatedAtDesc(Long userId, Long collectionId);
    List<Bookmark> findAllByUserIdAndCollectionIdOrderByCreatedAtAsc(Long userId, Long collectionId);

    @Query("select b from Bookmark b " +
            "where b.user.id = :userId and b.collection.id = :collectionId " +
            "order by " +
            "   case when b.title is null or b.title = '' then 1 else 0 end, b.title asc, b.createdAt desc")
    List<Bookmark> findAllSortedByTitle(@Param("userId") Long userId, @Param("collectionId") Long collectionId);

    long countByCollectionId(Long collectionId);

    @Query("select new com.linknest.backend.common.dto.IdCount(b.collection.id, count(b)) " +
            "from Bookmark b " +
            "where b.collection.id in :collectionIds " +
            "group by b.collection.id")
    List<IdCount> countByCollectionIds(@Param("collectionIds") List<Long> collectionIds);
}
