package com.linknest.backend.common.dto;

public record SliceMeta(
        int page,
        int size,
        boolean hasNext
) {}
