package com.linknest.backend.tag.unused;

import java.time.Instant;

public record UnusedTagResult(
        int movedToTrashCount,
        int scannedCount,
        Instant cutoff
) {}
