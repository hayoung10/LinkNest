package com.linknest.backend.bookmark.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BookmarkEmojiUpdateReq(
        @NotBlank(message = "이모지는 필수입니다.")
        @Size(max = 16)
        String emoji
) {}
