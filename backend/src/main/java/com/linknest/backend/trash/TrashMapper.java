package com.linknest.backend.trash;

import com.linknest.backend.collection.Collection;
import com.linknest.backend.tag.Tag;
import com.linknest.backend.trash.domain.TrashType;
import com.linknest.backend.trash.dto.TrashBookmarkRow;
import com.linknest.backend.trash.dto.TrashItemRes;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TrashMapper {
    default TrashItemRes fromCollection(Collection c) {
        return new TrashItemRes(
                TrashType.COLLECTION,
                c.getId(),
                c.getName(),
                null,
                c.getEmoji(),
                null,
                null,
                c.getDeletedAt(),
                null, // (enrich 단계에서 채움)
                null, // (enrich 단계에서 채움)
                null
        );
    }

    default TrashItemRes fromTag(Tag t) {
        return new TrashItemRes(
                TrashType.TAG,
                t.getId(),
                t.getName(),
                null,
                null,
                null,
                null,
                t.getDeletedAt(),
                null,
                null,
                null
        );
    }

    default TrashItemRes fromBookmarkRow(TrashBookmarkRow r) {
        return new TrashItemRes(
                TrashType.BOOKMARK,
                r.id(),
                r.title(),
                r.url(),
                r.emoji(),
                r.collectionName(),
                r.collectionEmoji(),
                r.deletedAt(),
                null,
                null,
                null
        );
    }
}
