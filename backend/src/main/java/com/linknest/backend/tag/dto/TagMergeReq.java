package com.linknest.backend.tag.dto;

import jakarta.validation.constraints.NotNull;

public record TagMergeReq(
        @NotNull
        Long targetTagId
) {}
