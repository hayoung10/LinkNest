package com.linknest.backend.collection;

import com.linknest.backend.bookmark.BookmarkRepository;
import com.linknest.backend.collection.dto.CollectionCreateReq;
import com.linknest.backend.collection.dto.CollectionRes;
import com.linknest.backend.collection.dto.CollectionUpdateReq;
import com.linknest.backend.common.exception.BusinessException;
import com.linknest.backend.common.exception.ErrorCode;
import com.linknest.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CollectionService {
    private final CollectionRepository collectionRepository;
    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final CollectionMapper mapper;

    // ---------- 생성 ----------
    @Transactional
    public CollectionRes create(Long userId, CollectionCreateReq req) {
        Collection parent = (req.parentId() == null)
                ? null : requireOwnedCollection(userId, req.parentId());

        int nextOrder = (parent == null)
                ? nextOrderForRoot(userId)
                : nextOrderForSiblings(userId, parent.getId());

        Collection collection = mapper.toEntity(req);
        collection.setUser(userRepository.getReferenceById(userId));
        collection.setParent(parent);
        collection.setSortOrder(nextOrder);

        Collection saved = collectionRepository.save(collection);
        return buildResWithCount(saved);
    }

    // ---------- 조회 ----------
    public CollectionRes get(Long userId, Long id) {
        Collection collection = requireOwnedCollection(userId, id);
        return buildResWithCount(collection);
    }

    // ---------- 수정 ----------
    @Transactional
    public CollectionRes update(Long userId, Long id, CollectionUpdateReq req) {
        Collection collection = requireOwnedCollection(userId, id);
        mapper.updateFromDto(req, collection);
        return mapper.toRes(collection);
    }

    // ---------- 삭제 ----------
    @Transactional
    public void delete(Long userId, Long id) {
        Collection collection = requireOwnedCollection(userId, id);
        collectionRepository.delete(collection);
    }

    // ---------- 하위 컬렉션 목록 조회 ----------
    public List<CollectionRes> listChildren(Long userId, Long parentId) {
        List<Collection> list = (parentId == null)
                ? collectionRepository.findAllByUserIdAndParentIsNullOrderBySortOrderAscCreatedAtAsc(userId)
                : collectionRepository.findAllByUserIdAndParentIdOrderBySortOrderAscCreatedAtAsc(userId, parentId);

        return list.stream().map(this::buildResWithCount).toList();
    }

    // ---------- 이동 (경로 변경) ----------
    @Transactional
    public void move(Long userId, Long id, Long targetParentId) {
        Collection newParent = (targetParentId == null) ? null : requireOwnedCollection(userId, targetParentId);
        Collection collection = requireOwnedCollection(userId, id);

        // 사이클 검사: newParent가 collection의 하위인 경우 -> 사이클 발생
        if(newParent != null && isDescendant(newParent, collection)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        int nextOrder = (newParent == null)
                ? nextOrderForRoot(userId)
                : nextOrderForSiblings(userId, newParent.getId());

        collection.setParent(newParent);
        collection.setSortOrder(nextOrder);
    }

    // ---------- 순서 변경 (같은 경로 내) ----------
    @Transactional
    public void reorder(Long userId, Long id, int newOrder) {
        Collection collection = requireOwnedCollection(userId, id);
        Long parentId = (collection.getParent() == null)
                ? null : collection.getParent().getId();

        List<Collection> siblings = (parentId == null)
                ? collectionRepository.findAllByUserIdAndParentIsNullOrderBySortOrderAscCreatedAtAsc(userId)
                : collectionRepository.findAllByUserIdAndParentIdOrderBySortOrderAscCreatedAtAsc(userId, parentId);

        // 재배치
        int oldIdx = siblings.indexOf(collection);
        siblings.remove(oldIdx);
        int idx = Math.max(0, Math.min(newOrder, siblings.size()));
        siblings.add(idx, collection);

        int from = Math.min(oldIdx, idx);
        for(int i = from; i < siblings.size(); i++) {
            siblings.get(i).setSortOrder(i);
        }
    }

    // ==========================================================
    // 내부 유틸
    // ==========================================================

    private Collection requireOwnedCollection(Long userId, Long id) {
        Collection collection =  collectionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.COLLECTION_NOT_FOUND));
        if(!collection.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        return collection;
    }

    private CollectionRes buildResWithCount(Collection c) {
        long count = bookmarkRepository.countByCollectionId(c.getId());
        return mapper.toResWithCount(c, count);
    }

    private int nextOrderForRoot(Long userId) { // 루트 컬렉션 중 가장 큰 sortOrder + 1 계산
        Integer max = collectionRepository.findMaxSortOrderByUserIdAndParentIsNull(userId);
        return (max == null ? 0 : max + 1);
    }

    private int nextOrderForSiblings(Long userId, Long parentId) { // 부모 컬렉션의 자식들 중 가장 큰 sotOrder + 1 계산
        Integer max = collectionRepository.findMaxSortOrderByUserIdAndParentId(userId, parentId);
        return (max == null ? 0 : max + 1);
    }

    private boolean isDescendant(Collection node, Collection ancestor) { // 자손 검사
        for(Collection cur = node.getParent(); cur != null; cur = cur.getParent()) {
            if(cur.getId().equals(ancestor.getId())) return true;
        }
        return false;
    }
}
