package com.linknest.backend.collection.dto.response;

public record CollectionNodeRes(
        Long id,
        String name,
        String emoji,
        Long parentId,
        int sortOrder,
        long bookmarkCount,
        long childCount
) {}
