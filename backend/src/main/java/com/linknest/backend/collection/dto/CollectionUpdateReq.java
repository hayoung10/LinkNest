package com.linknest.backend.collection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CollectionUpdateReq(
        @NotBlank(message = "컬렉션 이름을 입력하세요.")
        @Size(max = 255)
        String name
) {}
