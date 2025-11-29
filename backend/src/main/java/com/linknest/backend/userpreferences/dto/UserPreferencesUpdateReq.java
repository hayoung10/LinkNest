package com.linknest.backend.userpreferences.dto;

public record UserPreferencesUpdateReq(
        String defaultBookmarkSort,
        String defaultLayout,
        boolean openInNewTab,
        boolean keepSignedIn
) {
}
