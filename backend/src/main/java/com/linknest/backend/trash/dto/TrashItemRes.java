package com.linknest.backend.trash.dto;

import com.linknest.backend.trash.domain.TrashType;

import java.time.Instant;

public record TrashItemRes(
        TrashType type,

        Long id,
        String title,       // COLLECTION: name, BOOKMARK: title, TAG: name
        String subtitle,    // COLLECTION/TAG: null, BOOKMARK: url
        String emoji,       // COLLECTION/BOOKMARK: emoji, TAG: null

        String parentName,  // BOOKMARK only (collection name)
        String parentEmoji, // BOOKMARK only (collection emoji)

        Instant deletedAt,

        Long childCount,    // COLLECTION only
        Long bookmarkCount, // COLLECTION only
        Long taggedCount    // TAG only
) {}
