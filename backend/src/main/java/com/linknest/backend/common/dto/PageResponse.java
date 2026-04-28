package com.linknest.backend.common.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(
        List<T> items,
        PageMeta meta
) {
    public static <T> PageResponse<T> of(List<T> items, int page, int size, long totalElements, int totalPages) {
        return new PageResponse<>(
                items,
                new PageMeta(
                        page,
                        size,
                        totalElements,
                        totalPages
                )
        );
    }

    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                new PageMeta(
                        page.getNumber(),
                        page.getSize(),
                        page.getTotalElements(),
                        page.getTotalPages()
                )
        );
    }
}
