package com.linknest.backend.tag.mapper;

import com.linknest.backend.tag.dto.TaggedBookmarkRes;
import com.linknest.backend.tag.dto.TaggedBookmarkRow;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TaggedBookmarkMapper {
    // Row -> Res
    TaggedBookmarkRes toRes(TaggedBookmarkRow row);
}
