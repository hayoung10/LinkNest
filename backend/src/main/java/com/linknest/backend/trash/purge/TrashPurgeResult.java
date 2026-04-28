package com.linknest.backend.trash.purge;

import java.time.Instant;

public record TrashPurgeResult(
        int deletedCollections,
        int deletedBookmarks,
        int deletedTags,
        int scannedCollections,
        int scannedBookmarks,
        int scannedTags,
        Instant cutoff
) {}
