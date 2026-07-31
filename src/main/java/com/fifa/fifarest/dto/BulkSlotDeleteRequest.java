package com.fifa.fifarest.dto;

import com.fifa.fifarest.domain.TimeOfDay;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record BulkSlotDeleteRequest(
        @NotNull(message = "Year is required")
        @Min(value = 2000, message = "Year must be a valid year")
        Integer year,

        @NotNull(message = "Month is required")
        @Min(value = 1, message = "Month must be between 1 and 12")
        @Max(value = 12, message = "Month must be between 1 and 12")
        Integer month,

        @NotEmpty(message = "Select at least one time-of-day category to delete")
        Set<TimeOfDay> categories
) {
}
