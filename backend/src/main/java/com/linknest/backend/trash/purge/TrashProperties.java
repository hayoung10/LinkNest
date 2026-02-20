package com.linknest.backend.trash.purge;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.trash")
public record TrashProperties(
        boolean enabled,
        String cron,
        String zone,
        int batchSize,
        int ttlDays
) {}
