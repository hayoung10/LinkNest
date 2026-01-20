package com.linknest.backend.tag.dto;

public record TagRes(
        Long id,
        String name,
        long bookmarkCount
) {}
