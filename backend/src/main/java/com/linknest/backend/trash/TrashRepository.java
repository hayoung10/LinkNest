package com.linknest.backend.trash;

import com.linknest.backend.collection.Collection;
import com.linknest.backend.tag.Tag;
import com.linknest.backend.trash.dto.TrashBookmarkRow;

import java.util.List;

public interface TrashRepository {
    List<Collection> findDeletedCollections(Long userId, int offset, int limit);
    List<Tag> findDeletedTags(Long userId, int offset, int limit);

    List<TrashBookmarkRow> findDeletedBookmarks(Long userId, int offset, int limit);
}
