package com.linknest.backend.common.response;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        List<FieldError> errors
) {
    public static ErrorResponse of(int status, String code, String message, String path, List<FieldError> errors) {
        return new ErrorResponse(Instant.now(), status, code, message, path, errors);
    }

    public static ErrorResponse of(int status, String code, String message, String path) {
        return of(status, code, message, path, List.of());
    }

    public record FieldError(
            String field,
            String value,
            String reason
    ) {}
}
