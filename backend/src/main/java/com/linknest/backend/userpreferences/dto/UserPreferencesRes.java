package com.linknest.backend.userpreferences.dto;

public record UserPreferencesRes(
    String defaultBookmarkSort,
    String defaultLayout,
    boolean openInNewTab,
    boolean keepSignedIn
) {}
