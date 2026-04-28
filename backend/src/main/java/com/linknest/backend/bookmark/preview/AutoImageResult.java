package com.linknest.backend.bookmark.preview;

import com.linknest.backend.bookmark.Bookmark.AutoImageStatus;

public record AutoImageResult(
        AutoImageStatus status,
        String imageUrl
) {
    public static AutoImageResult success(String imageUrl) {
        return new AutoImageResult(AutoImageStatus.SUCCESS, imageUrl);
    }

    public static AutoImageResult failed() {
        return new AutoImageResult(AutoImageStatus.FAILED, null);
    }
}
