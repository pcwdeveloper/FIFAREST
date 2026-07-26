package com.fifa.fifarest.dto;

import com.fifa.fifarest.domain.VenueStatus;
import jakarta.validation.constraints.NotNull;

public record VenueStatusUpdateRequest(
        @NotNull(message = "Status is required")
        VenueStatus status
) {
}
