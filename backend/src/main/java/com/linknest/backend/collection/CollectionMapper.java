package com.linknest.backend.collection;

import com.linknest.backend.collection.dto.CollectionCreateReq;
import com.linknest.backend.collection.dto.CollectionRes;
import com.linknest.backend.collection.dto.CollectionUpdateReq;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface CollectionMapper {
    // CreateReq -> Entity
    Collection toEntity(CollectionCreateReq createReq);

    // UpdateReq -> Entity (null 값은 무시)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(CollectionUpdateReq updateReq, @MappingTarget Collection collection);

    // Entity -> Res
    CollectionRes toRes(Collection collection);
}
