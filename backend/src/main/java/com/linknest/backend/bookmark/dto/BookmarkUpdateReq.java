package com.linknest.backend.bookmark.dto;

import com.linknest.backend.bookmark.Bookmark.ImageMode;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record BookmarkUpdateReq(
        @Size(min = 2, max = 2048)
        @URL(message = "올바른 URL 형식이 아닙니다.")
        String url,

        @Size(max = 255)
        String title,

        @Size(max = 1000)
        String description,

        @Size(max = 16)
        String emoji,

        @Size(max = 1000)
        String customImageUrl,

        ImageMode imageMode
) {}
