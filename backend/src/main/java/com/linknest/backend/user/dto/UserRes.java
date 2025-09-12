package com.linknest.backend.user.dto;

import com.linknest.backend.user.User.Role;

import java.time.Instant;

public record UserRes(
        Long id,
        String email,
        String name,
        String profileImageUrl,
        Role role,
        Instant createdAt,
        Instant updatedAt
) {}
