package com.linknest.backend.collection;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CollectionRepository extends JpaRepository<Collection, Long> {
    // 루트 컬렉션 자식 목록
    List<Collection> findAllByUserIdAndParentIsNullOrderBySortOrderAscCreatedAtAsc(Long userId);

    // 컬렉션 자식 목록 (정렬)
    List<Collection> findByUserIdAndParentIdOrderBySortOrderAscCreatedAtAsc(Long userId, Long parentId);

    // 컬렉션 자식 목록
    List<Collection> findAllByUserIdAndParentId(Long userId, Long parentId);
}
