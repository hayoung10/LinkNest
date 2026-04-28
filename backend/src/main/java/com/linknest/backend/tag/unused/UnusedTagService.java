package com.linknest.backend.tag.unused;

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
public class UnusedTagService {
    private final TagRepository tagRepository;
    private final Clock clock;

    @Transactional
    public UnusedTagResult moveUnusedTagsToTrash(int batchSize, long ttlDays) {
        Instant now = Instant.now(clock);
        Instant cutoff = Instant.now(clock).minus(ttlDays, ChronoUnit.DAYS);

        // 휴지통 이동 대상 조회 (이미 휴지통인 태그는 제외)
        List<Long> ids = tagRepository.findOrphanTagIds(cutoff, batchSize);
        int scanned = ids.size();

        if(ids.isEmpty()) {
            log.debug("[UnusedTag] no orphan tags to move to trash. cutoff={}, batchSize={}, ttlDays={}",
                    cutoff, batchSize, ttlDays);
            return new UnusedTagResult(0, 0, cutoff);
        }

        // 휴지통 이동
        int moved = tagRepository.moveToTrashByIds(ids, now);

        log.debug("[UnusedTag] moved orphan tags to trash. moved={}, scanned={}, cutoff={}, batchSize={}, ttlDays={}",
                moved, scanned, cutoff, batchSize, ttlDays);

        return new UnusedTagResult(moved, scanned, cutoff);
    }
}
