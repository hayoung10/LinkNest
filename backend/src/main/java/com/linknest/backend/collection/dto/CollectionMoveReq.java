package com.linknest.backend.collection.dto;

import jakarta.validation.constraints.NotNull;

public record CollectionMoveReq(
        @NotNull(message = "이동할 부모 컬렉션 ID를 입력하세요.")
        Long targetParentId
){}
