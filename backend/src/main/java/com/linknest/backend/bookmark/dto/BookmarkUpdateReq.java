package com.linknest.backend.bookmark.dto;

import jakarta.validation.constraints.Size;

public record BookmarkUpdateReq(
    @Size(min = 2, max = 2048)
    String url,

    @Size(max = 255)
    String title,

    @Size(max = 1000)
    String description
) {}
