package com.linknest.backend.bookmark.event;

public record BookmarkAutoImageRequestedEvent(
        Long userId,
        Long bookmarkId,
        String url
) {}
