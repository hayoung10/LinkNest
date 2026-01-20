package com.linknest.backend.tag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TagUpdateReq(
        @NotBlank
        @Size(max = 50)
        String name
) {}
