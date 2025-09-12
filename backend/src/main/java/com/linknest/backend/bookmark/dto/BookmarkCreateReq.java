package com.linknest.backend.bookmark.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BookmarkCreateReq(
    @NotBlank(message = "url을 입력하세요.")
    @Size(min = 2, max = 2048)
    String url,

    @Size(max = 255)
    String title,

    @Size(max = 1000)
    String description
) {}
