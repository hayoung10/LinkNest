package com.linknest.backend.tag.cleanup;

import com.linknest.backend.tag.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TagCleanupService {
    private final TagRepository tagRepository;
    private final Clock clock;

    @Transactional
    public TagCleanupResult cleanupOrphanTags(int batchSize, long ttlDays) {
        Instant cutoff = Instant.now(clock).minus(1, ChronoUnit.DAYS);

        // 삭제 대상 조회
        List<Long> ids = tagRepository.findOrphanTagIds(cutoff, batchSize);
        int scanned = ids.size();

        if(ids.isEmpty()) {
            log.debug("[TagCleanup] no orphan tags to delete. cutoff={}, batchSize={}, ttlDays={}",
                    cutoff, batchSize, ttlDays);
            return new TagCleanupResult(0, 0, cutoff);
        }

        // 삭제
        tagRepository.deleteAllByIdInBatch(ids);
        int deleted = scanned;

        log.info("[TagCleanup] deleted orphan tags. deleted={}, scanned={}, cutoff={}, batchSize={}, ttlDays={}",
                deleted, scanned, cutoff, batchSize, ttlDays);

        return new TagCleanupResult(deleted, scanned, cutoff);
    }
}
