package com.linknest.backend.common.dto;

import org.springframework.data.domain.Slice;

import java.util.List;

public record SliceResponse<T>(
        List<T> items,
        SliceMeta meta
) {
    public static <T> SliceResponse<T> of(Slice<T> slice) {
        return new SliceResponse<>(
                slice.getContent(),
                new SliceMeta(
                        slice.getNumber(),
                        slice.getSize(),
                        slice.hasNext()
                )
        );
    }
}
