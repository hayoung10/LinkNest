package com.linknest.backend.collection;

import com.linknest.backend.collection.dto.CollectionCreateReq;
import com.linknest.backend.collection.dto.CollectionNodeRes;
import com.linknest.backend.collection.dto.CollectionRes;
import com.linknest.backend.collection.dto.CollectionUpdateReq;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CollectionMapper {
    // CreateReq -> Entity
    Collection toEntity(CollectionCreateReq createReq);

    // UpdateReq -> Entity (null 값은 무시)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(CollectionUpdateReq updateReq, @MappingTarget Collection collection);

    // Entity -> Res
    @Mapping(target = "bookmarkCount", constant = "java(0L)")
    @Mapping(target = "childCount", constant = "java(0L)")
    @Mapping(target = "parentId", expression = "java(c.getParent() == null ? null : c.getParent().getId())")
    CollectionRes toRes(Collection c);

    @Mapping(target = "parentId", expression = "java(c.getParent() == null ? null : c.getParent().getId())")
    @Mapping(target = "bookmarkCount", expression = "java(bookmarkCount)")
    @Mapping(target = "childCount", expression = "java(childCount)")
    CollectionRes toResWithCount(Collection c, long bookmarkCount, long childCount);

    @Mapping(target = "parentId", expression = "java(c.getParent() == null ? null : c.getParent().getId())")
    @Mapping(target = "bookmarkCount", expression = "java(bookmarkCount)")
    @Mapping(target = "childCount", expression = "java(childCount)")
    CollectionNodeRes toNodeRes(Collection c, long bookmarkCount, long childCount);
}
