package com.linknest.backend.tag.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record TagReplaceReq(
        @NotEmpty
        List<@Min(1) Long> bookmarkIds,

        @NotNull
        @Min(1)
        Long targetTagId
) {
        public TagReplaceReq {
                bookmarkIds = bookmarkIds.stream()
                        .distinct()
                        .toList();
        }
}
