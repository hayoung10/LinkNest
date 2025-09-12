package com.linknest.backend.user.dto;

import jakarta.validation.constraints.Size;

public record UserUpdateReq(
        @Size(max = 100) String name,
        @Size(max = 512) String profileImageUrl
) {}