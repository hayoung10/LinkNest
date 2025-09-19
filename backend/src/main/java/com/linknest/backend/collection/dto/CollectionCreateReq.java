package com.linknest.backend.collection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CollectionCreateReq(
        @NotBlank(message = "이름을 입력하세요.")
        @Size(min = 1, max = 255)
        String name,

        String icon,

        Long parentId
) {}
