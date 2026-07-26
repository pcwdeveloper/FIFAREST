package com.fifa.fifarest.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record SlotRequest(
        @NotNull(message = "Date is required")
        LocalDate date,

        @NotNull(message = "Start time is required")
        LocalTime startTime,

        @NotNull(message = "End time is required")
        LocalTime endTime
) {
}
