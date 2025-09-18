package com.linknest.backend.collection.dto;

import jakarta.validation.constraints.Size;

public record CollectionUpdateReq(
        @Size(min = 1, max = 255)
        String name,

        String icon
) {}
