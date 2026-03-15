package com.linknest.backend.trash.dto;

import com.linknest.backend.trash.domain.TrashType;

public record TrashBulkItemReq(
   TrashType type,
   Long id
) {}
