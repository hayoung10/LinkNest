package com.linknest.backend.collection.dto;

import java.time.Instant;

public record CollectionRes(
        Long id,
        String name,
        String icon,
        Long parentId,
        int sortOrder,
        Instant createdAt,
        Instant updatedAt,
        long bookmarkCount,
        long childCount
) {}
