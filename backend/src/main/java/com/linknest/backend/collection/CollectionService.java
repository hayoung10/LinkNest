package com.linknest.backend.collection;

import com.linknest.backend.collection.dto.CollectionCreateReq;
import com.linknest.backend.collection.dto.CollectionRes;
import com.linknest.backend.collection.dto.CollectionUpdateReq;
import com.linknest.backend.common.exception.BusinessException;
import com.linknest.backend.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CollectionService {
    private final CollectionRepository repository;
    private final CollectionMapper mapper;

    @Transactional
    public CollectionRes create(CollectionCreateReq req) {
        Collection collection = repository.save(mapper.toEntity(req));

        return mapper.toRes(collection);
    }

    public CollectionRes get(Long id) {
        return mapper.toRes(findVerifiedCollection(id));
    }

    @Transactional
    public CollectionRes update(Long id, CollectionUpdateReq req) {
        Collection collection = findVerifiedCollection(id);
        mapper.updateFromDto(req, collection);

        return mapper.toRes(collection);
    }

    @Transactional
    public void delete(Long id) {
        Collection collection = findVerifiedCollection(id);
        repository.delete(collection);
    }

    private Collection findVerifiedCollection(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.COLLECTION_NOT_FOUND));
    }
}
