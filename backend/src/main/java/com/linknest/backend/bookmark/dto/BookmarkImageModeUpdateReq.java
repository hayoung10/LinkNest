package com.linknest.backend.bookmark.dto;

import com.linknest.backend.bookmark.Bookmark.ImageMode;
import jakarta.validation.constraints.NotNull;

public record BookmarkImageModeUpdateReq(
        @NotNull
        ImageMode imageMode
) {}
