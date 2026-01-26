package com.linknest.backend.tag.dto;

import com.linknest.backend.bookmark.Bookmark.ImageMode;

import java.time.Instant;
import java.util.List;

public record TaggedBookmarkRow(
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
) {
    public TaggedBookmarkRow withTags(List<String> tags) {
        return new TaggedBookmarkRow(
                id, collectionId, collectionName, collectionEmoji,
                url, title, description, emoji,
                autoImageUrl, customImageUrl, imageMode, isFavorite,
                tags, createdAt, updatedAt
        );
    }
}
