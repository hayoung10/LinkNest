package com.linknest.backend.bookmark.dto;

import jakarta.validation.constraints.NotNull;

public record BookmarkMoveReq(
        @NotNull(message = "이동할 컬렉션 ID를 입력하세요.")
        Long targetCollectionId
) {}
