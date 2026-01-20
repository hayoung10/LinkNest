package com.linknest.backend.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        int status,
        String code,
        String message,
        T data
) {
    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(200, "OK", message, data);
    }

    public static ApiResponse<Void> ok(String message) {
        return new ApiResponse<>(200, "OK", message, null);
    }
}
