package com.linknest.backend.collection.dto;

public record CollectionMoveRes(
        Long id,
        Long parentId,
        int sortOrder
) {}
