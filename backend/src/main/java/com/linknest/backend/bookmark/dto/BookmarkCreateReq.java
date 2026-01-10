package com.linknest.backend.bookmark.dto;

import com.linknest.backend.bookmark.Bookmark.ImageMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.util.List;

public record BookmarkCreateReq(
        @NotNull(message = "컬렉션 ID를 입력하세요.")
        Long collectionId,

        @NotBlank(message = "url을 입력하세요.")
        @Size(min = 2, max = 2048)
        @URL(message = "올바른 URL 형식이 아닙니다.")
        String url,

        @Size(max = 255)
        String title,

        @Size(max = 1000)
        String description,

        @Size(max = 16)
        String emoji,

        ImageMode imageMode,

        boolean isFavorite,

        @Size(max = 3, message = "태그는 최대 3개까지 가능합니다.")
        List<@Size(max = 50) String> tags
) {}
