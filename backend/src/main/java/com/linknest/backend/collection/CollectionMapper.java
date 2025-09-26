package com.linknest.backend.collection;

import com.linknest.backend.collection.dto.CollectionCreateReq;
import com.linknest.backend.collection.dto.CollectionRes;
import com.linknest.backend.collection.dto.CollectionUpdateReq;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CollectionMapper {
    // CreateReq -> Entity
    Collection toEntity(CollectionCreateReq createReq);

    // UpdateReq -> Entity (null 값은 무시)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(CollectionUpdateReq updateReq, @MappingTarget Collection collection);

    // Entity -> Res
    @Mapping(target = "bookmarkCount", constant = "0L")
    @Mapping(target = "parentId", source = "parent.id")
    CollectionRes toRes(Collection collection);

    @InheritConfiguration(name = "toRes")
    @Mapping(target = "bookmarkCount", expression = "java(count)")
    CollectionRes toResWithCount(Collection c, long count);
}
