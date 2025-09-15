package com.linknest.backend.common.dto;

public record PageMeta(
        int page,
        int size,
        long totalElements,
        int totalpages
) {}