package com.linknest.backend.trash.purge;

import com.linknest.backend.bookmark.BookmarkRepository;
import com.linknest.backend.collection.CollectionRepository;
import com.linknest.backend.tag.TagRepository;
import com.linknest.backend.tag.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrashPurgeService {
    private static final int MAX_ROUNDS = 3;

    private final Clock clock;

    private final CollectionRepository collectionRepository;
    private final BookmarkRepository bookmarkRepository;
    private final TagRepository tagRepository;

    private final TagService tagService;

    @Transactional
    public TrashPurgeResult purgeExpired(int batchSize, long ttlDays) {
        Instant now = Instant.now(clock);
        Instant cutoff = now.minus(ttlDays, ChronoUnit.DAYS);

        int totalDeletedCollections = 0;
        int totalDeletedBookmarks = 0;
        int totalDeletedTags = 0;

        int totalScannedCollections = 0;
        int totalScannedBookmarks = 0;
        int totalScannedTags = 0;

        for(int round = 1; round <= MAX_ROUNDS; round++) {
            Set<Long> affectedTagIds = new HashSet<>();

            // 만료된 루트 컬렉션 purge
            PurgeCount collections = purgeExpiredCollections(batchSize, cutoff, affectedTagIds);
            totalDeletedCollections += collections.deleted();
            totalScannedCollections += collections.scanned();

            // 만료된 북마크 purge
            PurgeCount bookmarks = purgeExpiredBookmarks(batchSize, cutoff, affectedTagIds);
            totalDeletedBookmarks += bookmarks.deleted();
            totalScannedBookmarks += bookmarks.scanned();

            // 만료된 태그 purge
            PurgeCount tags = purgeExpiredTags(batchSize, cutoff, affectedTagIds);
            totalDeletedTags += tags.deleted();
            totalScannedTags += tags.scanned();

            // orphanedAt 처리
            if (!affectedTagIds.isEmpty()) {
                tagService.onTagsDetached(affectedTagIds, now);
            }

            log.info("[TrashPurge] round {} done. collections={}, bookmarks={}, tags={}",
                    round, collections.deleted(), bookmarks.deleted(), tags.deleted());

            if(collections.deleted == 0 && bookmarks.deleted() == 0 && tags.deleted() == 0) {
                break;
            }
        }

        log.info("[TrashPurge] finished. cutoff={}, totalDeletedCollections={}, totalDeletedBookmarks={}, totalDeletedTags={}",
                cutoff, totalDeletedCollections, totalDeletedBookmarks, totalDeletedTags);

        return new TrashPurgeResult(
                totalDeletedCollections,
                totalDeletedBookmarks,
                totalDeletedTags,
                totalScannedCollections,
                totalScannedBookmarks,
                totalScannedTags,
                cutoff
        );
    }

    private PurgeCount purgeExpiredCollections(int batchSize, Instant cutoff, Set<Long> affectedTagIds) {
        List<Long> ids = collectionRepository.findExpiredDeletedRootIds(cutoff, batchSize);
        int scanned = ids.size();
        if(ids.isEmpty()) return new PurgeCount(0, 0);

        List<Long> childIds = collectionRepository.findSubtreeIdsByRootIds(ids);
        if(!childIds.isEmpty()) {
            affectedTagIds.addAll(bookmarkRepository.findTagIdsByDeletedBookmarksInCollectionIds(childIds));
        }

        int deleted = collectionRepository.deleteDeletedByIdIn(ids);

        return new PurgeCount(deleted, scanned);
    }

    private PurgeCount purgeExpiredBookmarks(int batchSize, Instant cutoff, Set<Long> affectedTagIds) {
        List<Long> ids = bookmarkRepository.findExpiredDeletedBookmarkIds(cutoff, batchSize);
        int scanned = ids.size();
        if(ids.isEmpty()) return new PurgeCount(0, 0);

        affectedTagIds.addAll(bookmarkRepository.findTagIdsByDeletedBookmarkIds(ids));

        int deleted = bookmarkRepository.deleteDeletedByIdIn(ids);

        return new PurgeCount(deleted, scanned);
    }

    private PurgeCount purgeExpiredTags(int batchSize, Instant cutoff, Set<Long> affectedTagIds) {
        List<Long> ids = tagRepository.findExpiredDeletedTagIds(cutoff, batchSize);
        int scanned = ids.size();
        if(ids.isEmpty()) return new PurgeCount(0, 0);

        int deleted = tagRepository.deleteDeletedByIdIn(ids);

        return new PurgeCount(deleted, scanned);
    }

    private record PurgeCount(int deleted, int scanned) {}
}
