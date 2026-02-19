package com.linknest.backend.collection;

import com.linknest.backend.common.dto.IdCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CollectionRepository extends JpaRepository<Collection, Long> {
    // 루트 컬렉션 자식 목록
    List<Collection> findAllByUserIdAndParentIsNullOrderBySortOrderAscCreatedAtAsc(Long userId);

    // 컬렉션 자식 목록 (정렬)
    List<Collection> findAllByUserIdAndParentIdOrderBySortOrderAscCreatedAtAsc(Long userId, Long parentId);

    @Query("select max(c.sortOrder) from Collection c where c.user.id = :userId and c.parent is null")
    Integer findMaxSortOrderByUserIdAndParentIsNull(@Param("userId") Long userId);

    @Query("select max(c.sortOrder) from Collection c where c.user.id = :userId and c.parent.id = :parentId")
    Integer findMaxSortOrderByUserIdAndParentId(@Param("userId") Long userId, @Param("parentId") Long parentId);

    long countByUserIdAndParentId(Long userId, Long parentId);

    Optional<Collection> findByIdAndUserId(Long id, Long userId);

    @Query(value = "select * from collections where id = :id and user_id = :userId", nativeQuery = true)
    Optional<Collection> findIncludingDeletedByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Query("select new com.linknest.backend.common.dto.IdCount(c.parent.id, count(c)) " +
            "from Collection c " +
            "where c.user.id = :userId and c.parent.id in :parentIds " +
            "group by c.parent.id")
    List<IdCount> countChildrenByParentIds(@Param("userId") Long userId, @Param("parentIds") List<Long> parentIds);

    List<Collection> findAllByUserIdOrderByParentIdAscSortOrderAsc(Long userId);

    Optional<Collection> findByUserIdAndParentIsNullAndName(Long userId, String name);

    // -------------------- Bulk Ops --------------------
    @Query(value =
            "with recursive tree AS (" +
            "   select id from collections " +
            "   where id = :rootId and user_id = :userId and deleted_at is null " +
            "   union all " +
            "   select c.id from collections c " +
            "       join tree t on c.parent_id = t.id" +
            "   where c.user_id = :userId and c.deleted_at is null" +
            ")" +
            "select id from tree", nativeQuery = true)
    List<Long> findSubtreeIds(@Param("userId") Long userId, @Param("rootId") Long rootId);

    @Modifying
    @Query(value =
            "update collections set deleted_at = now(6) " +
            "   where user_id = :userId and id in (:ids) and deleted_at is null", nativeQuery = true)
    int softDeleteAllByIds(@Param("userId") Long userId, @Param("ids") List<Long> ids);

    // -------------------- Trash --------------------
    @Modifying
    @Query(value = "delete from collections where user_id = :userId and deleted_at is not null", nativeQuery = true)
    int deleteAllDeletedByUserId(Long userId);

    @Modifying
    @Query(value = "delete from collections " +
            "where user_id = :userId " +
            "   and deleted_at is not null " +
            "   and id in (:ids)", nativeQuery = true)
    int deleteDeletedByUserIdAndIdIn(Long userId, List<Long> ids);

    @Query(value = "select * from collections " +
            "where user_id = :userId and parent_id is null and name = :name " +
            "limit 1", nativeQuery = true)
    Optional<Collection> findIncludingDeletedDefault(@Param("userId") Long userId, @Param("name") String name);
}
