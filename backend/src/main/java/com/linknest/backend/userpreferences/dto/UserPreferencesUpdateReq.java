package com.linknest.backend.userpreferences.dto;

public record UserPreferencesUpdateReq(
        String defaultBookmarkSort,
        String defaultLayout,
        Boolean openInNewTab,
        Boolean keepSignedIn
) {
}
