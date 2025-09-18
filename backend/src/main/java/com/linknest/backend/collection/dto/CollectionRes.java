package com.linknest.backend.collection.dto;

import java.time.Instant;

public record CollectionRes(
        Long id,
        String name,
        String icon,

        Instant createdAt,
        Instant updatedAt
) {}
