package com.linknest.backend.tag.cleanup;

import java.time.Instant;

public record TagCleanupResult(
        int deletedCount,
        int scannedCount,
        Instant cutoff
) {}
