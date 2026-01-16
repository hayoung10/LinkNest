package com.linknest.backend.tag.cleanup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@ConditionalOnProperty(name = "app.tag-cleanup.enabled", havingValue = "true")
@Component
@RequiredArgsConstructor
public class TagCleanupScheduler {
    private final TagCleanupService tagCleanupService;
    private final TagCleanupProperties properties;

    @Scheduled(cron = "${app.tag-cleanup.cron}", zone = "${app.tag-cleanup.zone:Asia/Seoul}")
    public void run() {
        try {
            TagCleanupResult result = tagCleanupService.cleanupOrphanTags(
                    properties.batchSize(),
                    properties.ttlDays()
            );

            log.info("[TagCleanup] done. deleted={}, scanned={}, cutoff={}",
                    result.deletedCount(),
                    result.scannedCount(),
                    result.cutoff());
        } catch (Exception e) {
            log.error("[TagCleanup] failed", e);
        }
    }
}
