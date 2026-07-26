package com.fifa.fifarest.dto;

import com.fifa.fifarest.domain.SlotStatus;
import jakarta.validation.constraints.NotNull;

public record SlotStatusUpdateRequest(
        @NotNull(message = "Status is required")
        SlotStatus status
) {
}
