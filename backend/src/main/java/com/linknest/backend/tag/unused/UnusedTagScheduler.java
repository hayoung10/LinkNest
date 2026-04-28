package com.linknest.backend.tag.unused;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@ConditionalOnProperty(name = "app.tag-unused.enabled", havingValue = "true")
@Component
@RequiredArgsConstructor
public class UnusedTagScheduler {
    private final UnusedTagService unusedTagService;
    private final UnusedTagProperties properties;

    @Scheduled(cron = "${app.tag-unused.cron}", zone = "${app.tag-unused.zone:Asia/Seoul}")
    public void run() {
        try {
            UnusedTagResult result = unusedTagService.moveUnusedTagsToTrash(
                    properties.batchSize(),
                    properties.ttlDays()
            );

            log.info("[UnusedTag] done. movedToTrash={}, scanned={}, cutoff={}",
                    result.movedToTrashCount(),
                    result.scannedCount(),
                    result.cutoff());
        } catch (Exception e) {
            log.error("[UnusedTag] failed", e);
        }
    }
}
