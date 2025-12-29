package com.linknest.backend.collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CollectionRepository extends JpaRepository<Collection, Long> {
    // 루트 컬렉션 자식 목록
    List<Collection> findAllByUserIdAndParentIsNullOrderBySortOrderAscCreatedAtAsc(Long userId);

    // 컬렉션 자식 목록
    List<Collection> findAllByUserIdAndParentId(Long userId, Long parentId);

    // 컬렉션 자식 목록 (정렬)
    List<Collection> findAllByUserIdAndParentIdOrderBySortOrderAscCreatedAtAsc(Long userId, Long parentId);

    @Query("select max(c.sortOrder) from Collection c where c.user.id = :userId and c.parent is null")
    Integer findMaxSortOrderByUserIdAndParentIsNull(@Param("userId") Long userId);

    @Query("select max(c.sortOrder) from Collection c where c.user.id = :userId and c.parent.id = :parentId")
    Integer findMaxSortOrderByUserIdAndParentId(@Param("userId") Long userId, @Param("parentId") Long parentId);

    long countByUserIdAndParentId(Long userId, Long parentId);

    Optional<Collection> findByIdAndUserId(Long id, Long userId);
}
