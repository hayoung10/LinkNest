package com.linknest.backend.bookmark.dto;

import com.linknest.backend.bookmark.Bookmark.ImageMode;

import java.time.Instant;
import java.util.List;

public record BookmarkRes(
    Long id,
    Long collectionId,
    String url,
    String title,
    String description,

    String emoji,
    String autoImageUrl,
    String customImageUrl,
    ImageMode imageMode,

    boolean isFavorite,

    List<String> tags,

    Instant createdAt,
    Instant updatedAt
) {}
