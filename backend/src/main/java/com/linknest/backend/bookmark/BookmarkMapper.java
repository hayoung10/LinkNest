package com.linknest.backend.bookmark;

import com.linknest.backend.bookmark.dto.BookmarkCreateReq;
import com.linknest.backend.bookmark.dto.BookmarkRes;
import com.linknest.backend.bookmark.dto.BookmarkUpdateReq;
import com.linknest.backend.tag.Tag;
import org.mapstruct.*;

import java.util.List;
import java.util.Objects;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookmarkMapper {
    // CreateReq -> Entity
    Bookmark toEntity(BookmarkCreateReq createReq);

    // UpdateReq -> Entity
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(BookmarkUpdateReq updateReq, @MappingTarget Bookmark target);

    // Entity -> Res
    default  BookmarkRes toRes(Bookmark bookmark) {
        if(bookmark == null) return null;

        List<String> tags = bookmark.getBookmarkTags().stream()
                .map(BookmarkTag::getTag)
                .filter(Objects::nonNull)
                .map(Tag::getName)
                .toList();

        return new BookmarkRes(
                bookmark.getId(),
                bookmark.getCollection().getId(),
                bookmark.getUrl(),
                bookmark.getTitle(),
                bookmark.getDescription(),
                bookmark.getEmoji(),
                bookmark.getAutoImageUrl(),
                bookmark.getCustomImageUrl(),
                bookmark.getImageMode(),
                bookmark.isFavorite(),
                tags,
                bookmark.getCreatedAt(),
                bookmark.getUpdatedAt()
        );
    }
}
