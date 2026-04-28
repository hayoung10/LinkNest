package com.linknest.backend.collection.dto.response;

import java.time.Instant;

public record CollectionRes(
        Long id,
        String name,
        String emoji,
        Long parentId,
        int sortOrder,
        Instant createdAt,
        Instant updatedAt,
        long bookmarkCount,
        long childCount
) {}
