package com.linknest.backend.tag.dto;

import java.time.Instant;

public record TagRes(
        Long id,
        String name,
        Instant createdAt,
        Instant updatedAt,
        long bookmarkCount
) {}
