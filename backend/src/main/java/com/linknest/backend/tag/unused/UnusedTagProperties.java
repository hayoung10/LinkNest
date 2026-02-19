package com.linknest.backend.tag.unused;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.tag-unused")
public record UnusedTagProperties(
        boolean enabled,
        String cron,
        String zone,
        int batchSize,
        int ttlDays
) {}
