package com.linknest.backend.bookmark.dto;

import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.util.List;

public record BookmarkUpdateReq(
        @Size(min = 2, max = 2048)
        @URL(message = "올바른 URL 형식이 아닙니다.")
        String url,

        @Size(max = 255)
        String title,

        @Size(max = 1000)
        String description,

        @Size(max = 3, message = "태그는 최대 3개까지 가능합니다.")
        List<@Size(max = 50) String> tags
) {}
