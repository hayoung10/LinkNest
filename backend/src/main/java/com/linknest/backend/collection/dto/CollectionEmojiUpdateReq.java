package com.linknest.backend.collection.dto;

import jakarta.validation.constraints.Size;

public record CollectionEmojiUpdateReq(
   @Size(max = 16)
   String emoji
) {}
