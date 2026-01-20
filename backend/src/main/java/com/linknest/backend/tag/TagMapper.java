package com.linknest.backend.tag;

import com.linknest.backend.tag.dto.TagRes;
import com.linknest.backend.tag.dto.TagUpdateReq;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TagMapper {
    // UpdateReq -> Entity
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(TagUpdateReq updateReq, @MappingTarget Tag tag);

    // Entity -> Res
    @Mapping(target = "bookmarkCount", constant = "0L")
    TagRes toRes(Tag tag);

    // Entity + count -> Res
    @Mapping(target = "bookmarkCount", expression = "java(bookmarkCount)")
    TagRes toResWithCount(Tag tag, long bookmarkCount);
}
