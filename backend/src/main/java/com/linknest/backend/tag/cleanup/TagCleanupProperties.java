package com.linknest.backend.tag.cleanup;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.tag-cleanup")
public record TagCleanupProperties(
        boolean enabled,
        String cron,
        String zone,
        int batchSize,
        int ttlDays
) {}
