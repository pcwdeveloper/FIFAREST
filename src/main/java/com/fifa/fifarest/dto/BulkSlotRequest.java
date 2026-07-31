package com.fifa.fifarest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalTime;

public record BulkSlotRequest(
        @NotNull(message = "Year is required")
        @Min(value = 2000, message = "Year must be a valid year")
        Integer year,

        @NotNull(message = "Month is required")
        @Min(value = 1, message = "Month must be between 1 and 12")
        @Max(value = 12, message = "Month must be between 1 and 12")
        Integer month,

        @NotNull(message = "Interval is required")
        @Min(value = 15, message = "Interval must be at least 15 minutes")
        Integer intervalMinutes,

        @NotNull @Valid CategoryConfig morning,
        @NotNull @Valid CategoryConfig afternoon,
        @NotNull @Valid CategoryConfig evening,
        @NotNull @Valid CategoryConfig night
) {
    public record CategoryConfig(
            boolean enabled,
            LocalTime startTime,
            LocalTime endTime,
            BigDecimal price
    ) {
    }
}
