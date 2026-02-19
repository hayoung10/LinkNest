package com.linknest.backend.trash.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record TrashBulkReq(
        @NotNull @NotEmpty List<Long> ids
) {}
