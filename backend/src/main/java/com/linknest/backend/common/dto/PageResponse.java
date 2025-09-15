package com.linknest.backend.common.dto;

import java.util.List;

public record PageResponse<T>(
        List<T> items,
        PageMeta meta
) {
    public static <T> PageResponse<T> of(List<T> items, int page, int size, long totalElements, int totalPages) {
        return new PageResponse<>(items, new PageMeta(page, size, totalElements, totalPages));
    }
}
