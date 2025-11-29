package com.linknest.backend.bookmark;

import com.linknest.backend.bookmark.dto.BookmarkCreateReq;
import com.linknest.backend.bookmark.dto.BookmarkRes;
import com.linknest.backend.bookmark.dto.BookmarkUpdateReq;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookmarkMapper {
    // CreateReq -> Entity
    Bookmark toEntity(BookmarkCreateReq createReq);

    // UpdateReq -> Entity (null 값은 무시)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    void updateFromDto(BookmarkUpdateReq updateReq, @MappingTarget Bookmark target);

    // Entity -> Res
    @Mapping(target = "collectionId", source = "collection.id")
    BookmarkRes toRes(Bookmark bookmark);
}
