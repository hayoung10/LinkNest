package com.linknest.backend.collection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CollectionUpdateReq(
        @Size(max = 255)
        String name,

        @Size(max = 16)
        String emoji
) {}
