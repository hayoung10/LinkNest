package com.linknest.backend.user.dto;

import com.linknest.backend.user.User.Role;
import com.linknest.backend.user.domain.AuthProvider;

import java.time.Instant;

public record UserRes(
        Long id,
        String email,
        String name,
        String profileImageUrl,
        Role role,
        AuthProvider provider,
        Instant createdAt,
        Instant updatedAt,
        long bookmarkCount,
        long collectionCount
) {}
