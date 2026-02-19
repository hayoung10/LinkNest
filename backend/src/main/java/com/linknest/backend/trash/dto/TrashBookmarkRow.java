package com.linknest.backend.trash.dto;

import java.time.Instant;

public record TrashBookmarkRow(
        Long id,
        String url,
        String title,
        String emoji,

        Instant deletedAt,

        Long collectionId,
        String collectionName,
        String collectionEmoji
) {}
