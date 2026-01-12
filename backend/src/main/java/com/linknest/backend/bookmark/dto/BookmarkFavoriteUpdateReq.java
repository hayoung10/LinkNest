package com.linknest.backend.bookmark.dto;

import jakarta.validation.constraints.NotNull;

public record BookmarkFavoriteUpdateReq(
        @NotNull Boolean isFavorite
) {}
