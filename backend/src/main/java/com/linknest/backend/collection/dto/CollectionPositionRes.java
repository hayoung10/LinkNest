package com.linknest.backend.collection.dto;

public record CollectionPositionRes(
        Long id,
        Long parentId,
        int sortOrder
) {}
