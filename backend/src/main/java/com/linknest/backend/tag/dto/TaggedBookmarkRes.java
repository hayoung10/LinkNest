package com.linknest.backend.tag.dto;

import com.linknest.backend.bookmark.Bookmark.ImageMode;

import java.time.Instant;
import java.util.List;

public record TaggedBookmarkRes(
        Long id,

        Long collectionId,
        String collectionName,
        String collectionEmoji,

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
