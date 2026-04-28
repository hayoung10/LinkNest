package com.linknest.backend.collection.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CollectionReorderReq(
        @NotNull(message = "이동할 위치(targetIndex)를 입력하세요.")
        @Min(value = 0, message = "targetIndex는 0 이상이어야 합니다.")
        Integer targetIndex
){}
