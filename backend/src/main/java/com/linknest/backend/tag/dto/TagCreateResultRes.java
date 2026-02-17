package com.linknest.backend.tag.dto;

public record TagCreateResultRes(
        TagRes res,
        boolean restored
) {}
