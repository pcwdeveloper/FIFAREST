package com.fifa.fifarest.dto;

import jakarta.validation.constraints.NotNull;

public record CreateBookingRequest(
        @NotNull(message = "Slot is required")
        Long slotId
) {
}
