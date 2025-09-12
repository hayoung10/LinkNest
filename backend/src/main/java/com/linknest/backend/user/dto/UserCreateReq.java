package com.linknest.backend.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateReq(
        @Email @NotBlank String email,
        @Size(max = 100) String name,
        @Size(max = 512) String profileImageUrl
) {}
