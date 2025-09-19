package com.linknest.backend.collection.dto;

import jakarta.validation.constraints.NotNull;

public record CollectionReorderReq(
        @NotNull(message = "새 순서를 입력하세요.")
        int newOrder
){}
