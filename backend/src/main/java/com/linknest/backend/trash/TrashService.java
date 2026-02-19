package com.linknest.backend.trash;

import com.linknest.backend.bookmark.Bookmark;
import com.linknest.backend.bookmark.BookmarkService;
import com.linknest.backend.collection.Collection;
import com.linknest.backend.collection.CollectionService;
import com.linknest.backend.common.dto.SliceResponse;
import com.linknest.backend.tag.Tag;
import com.linknest.backend.tag.TagService;
import com.linknest.backend.trash.domain.TrashType;
import com.linknest.backend.trash.dto.TrashBookmarkRow;
import com.linknest.backend.trash.dto.TrashItemRes;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrashService {
    private final TrashRepository trashRepository;

    private final CollectionService collectionService;
    private final BookmarkService bookmarkService;
    private final TagService tagService;

    private final TrashMapper trashMapper;

    public SliceResponse<TrashItemRes> list(Long userId, TrashType type, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 100);

        if(type != null) {
            return listByType(userId, type, safePage, safeSize);
        }

        // 통합 목록
        int offset = safePage * safeSize;
        int fetchSize = offset + safeSize + 1; // hasNext 판단을 위해 +1

        List<TrashItemRes> merged = new ArrayList<>(fetchSize *  3);

        List<Collection> collections = trashRepository.findDeletedCollections(userId, 0, fetchSize);
        List<TrashBookmarkRow> bookmarks = trashRepository.findDeletedBookmarks(userId, 0, fetchSize);
        List<Tag> tags = trashRepository.findDeletedTags(userId, 0, fetchSize);

        collections.forEach(c -> merged.add(trashMapper.fromCollection(c)));
        bookmarks.forEach(r -> merged.add(trashMapper.fromBookmarkRow(r)));
        tags.forEach(t -> merged.add(trashMapper.fromTag(t)));

        merged.sort(Comparator
                .comparing(TrashItemRes::deletedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(TrashItemRes::id, Comparator.nullsLast(Comparator.reverseOrder()))
        );

        if(merged.size() <= offset) {
            Slice<TrashItemRes> empty = new SliceImpl<>(List.of(), PageRequest.of(safePage, safeSize), false);
            return SliceResponse.of(empty);
        }

        int toIndex = Math.min(merged.size(), offset + safeSize + 1);
        List<TrashItemRes> content = merged.subList(offset, toIndex);

        boolean hasNext = content.size() > safeSize;
        List<TrashItemRes> result = hasNext ? content.subList(0, safeSize) : content;

        Slice<TrashItemRes> slice = new SliceImpl<>(result, PageRequest.of(safePage, safeSize), hasNext);
        return SliceResponse.of(slice);
    }

    @Transactional
    public void empty(Long userId, TrashType type) {
        if(type == null) {
            collectionService.deleteAllFromTrash(userId);
            bookmarkService.deleteAllFromTrash(userId);
            tagService.deleteAllFromTrash(userId);
            return;
        }

        switch(type) {
            case COLLECTION -> collectionService.deleteAllFromTrash(userId);
            case BOOKMARK -> bookmarkService.deleteAllFromTrash(userId);
            case TAG -> tagService.deleteAllFromTrash(userId);
        }
    }

    @Transactional
    public void restore(Long userId, TrashType type, Long id) {
        switch(type) {
            case COLLECTION -> collectionService.restoreFromTrash(userId, id);
            case BOOKMARK -> bookmarkService.restoreFromTrash(userId, id);
            case TAG -> tagService.restoreFromTrash(userId, id);
        }
    }

    @Transactional
    public void delete(Long userId, TrashType type, Long id) {
        switch(type) {
            case COLLECTION -> collectionService.deleteFromTrash(userId, id);
            case BOOKMARK -> bookmarkService.deleteFromTrash(userId, id);
            case TAG -> tagService.deleteFromTrash(userId, id);
        }
    }

    @Transactional
    public void restoreBulk(Long userId, TrashType type, List<Long> ids) {
        if(ids == null || ids.isEmpty()) return;

        switch(type) {
            case COLLECTION -> {
                for(Long id : ids) collectionService.restoreFromTrash(userId, id);
            }
            case BOOKMARK -> bookmarkService.restoreFromTrashBulk(userId, ids);
            case TAG -> tagService.restoreFromTrashBulk(userId, ids);
        }
    }

    @Transactional
    public void deleteBulk(Long userId, TrashType type, List<Long> ids) {
        if(ids == null || ids.isEmpty()) return;

        switch(type) {
            case COLLECTION -> collectionService.deleteFromTrashBulk(userId, ids);
            case BOOKMARK -> bookmarkService.deleteFromTrashBulk(userId, ids);
            case TAG -> tagService.deleteFromTrashBulk(userId, ids);
        }
    }


    // ==========================================================
    // 내부 유틸
    // ==========================================================
    private SliceResponse<TrashItemRes> listByType(Long userId, TrashType type, int safePage, int safeSize) {
        int offset = safePage * safeSize;
        int limit = safeSize + 1;

        return switch(type) {
            case COLLECTION -> {
                List<Collection> rows = trashRepository.findDeletedCollections(userId, offset, limit);
                yield SliceResponse.of(toSlice(rows, safePage, safeSize).map(trashMapper::fromCollection));
            }
            case BOOKMARK -> {
                List<TrashBookmarkRow> rows = trashRepository.findDeletedBookmarks(userId, offset, limit);
                yield SliceResponse.of(toSlice(rows, safePage, safeSize).map(trashMapper::fromBookmarkRow));
            }
            case TAG -> {
                List<Tag> rows = trashRepository.findDeletedTags(userId, offset, limit);
                yield SliceResponse.of(toSlice(rows, safePage, safeSize).map(trashMapper::fromTag));
            }
        };
    }

    private <T> Slice<T> toSlice(List<T> rows, int page, int size) {
        boolean hasNext = rows.size() > size;
        List<T> content = hasNext ? rows.subList(0, size) : rows;
        Pageable pageable = PageRequest.of(page, size);
        return new SliceImpl<>(content, pageable, hasNext);
    }
}
