package com.linknest.backend.auth.web.dto;

public record TokenRefreshRes(
        String accessToken,
        String tokenType,
        long expiresIn
) {}
