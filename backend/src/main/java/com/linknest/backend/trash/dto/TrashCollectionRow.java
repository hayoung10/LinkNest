package com.linknest.backend.trash.dto;

import java.time.Instant;

public record TrashCollectionRow(
        Long id,
        String name,
        String emoji,

        String parentName,
        String parentEmoji,

        Instant deletedAt
) {}
