package com.linknest.backend.bookmark.dto;

import java.time.Instant;

public record BookmarkRes(
    Long id,
    String url,
    String title,
    String description,

    Instant createdAt,
    Instant updatedAt
) {}
