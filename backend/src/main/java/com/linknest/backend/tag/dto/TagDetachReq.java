package com.linknest.backend.tag.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record TagDetachReq(
        @NotEmpty
        List<@Min(1) Long> bookmarkIds
) {
        public TagDetachReq {
                bookmarkIds = bookmarkIds.stream()
                        .distinct()
                        .toList();
        }
}
