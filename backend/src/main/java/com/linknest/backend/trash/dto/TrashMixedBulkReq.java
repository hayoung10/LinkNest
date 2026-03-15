package com.linknest.backend.trash.dto;

import java.util.List;

public record TrashMixedBulkReq(
        List<TrashBulkItemReq> items
) {}
