package com.linknest.backend.trash.purge;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@ConditionalOnProperty(name = "app.trash.enabled", havingValue = "true")
@Component
@RequiredArgsConstructor
public class TrashPurgeScheduler {
    private final TrashPurgeService trashPurgeService;
    private final TrashProperties properties;

    @Scheduled(cron = "${app.trash.cron}", zone = "${app.trash.zone:Asia/Seoul}")
    public void run() {
        try {
            TrashPurgeResult result = trashPurgeService.purgeExpired(
                    properties.batchSize(),
                    properties.ttlDays()
            );

            log.info("[TrashPurge] done. deletedCollections={}, deletedBookmarks={}, deletedTags={}, cutoff={}",
                    result.deletedCollections(),
                    result.deletedBookmarks(),
                    result.deletedTags(),
                    result.cutoff());
        } catch (Exception e) {
            log.error("[TrashPurge] failed", e);
        }
    }
}
