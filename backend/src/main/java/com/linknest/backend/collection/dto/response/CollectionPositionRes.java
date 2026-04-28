package com.linknest.backend.collection.dto.response;

public record CollectionPositionRes(
        Long id,
        Long parentId,
        int sortOrder
) {}
