package com.linknest.backend.trash;

import com.linknest.backend.tag.Tag;
import com.linknest.backend.trash.dto.TrashBookmarkRow;
import com.linknest.backend.trash.dto.TrashCollectionRow;

import java.util.List;

public interface TrashRepository {
    List<TrashCollectionRow> findDeletedCollections(Long userId, int offset, int limit);
    List<Tag> findDeletedTags(Long userId, int offset, int limit);

    List<TrashBookmarkRow> findDeletedBookmarks(Long userId, int offset, int limit);
}
