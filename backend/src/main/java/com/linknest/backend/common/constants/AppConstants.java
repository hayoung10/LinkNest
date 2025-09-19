package com.linknest.backend.common.constants;

public final class AppConstants {
    private AppConstants() {}

    // Security
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";

    // API
    public static final String API_PREFIX = "/api/v1";

    // Pagination
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;
}
