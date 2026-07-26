package com.fifa.fifarest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CourtRequest(
        @NotBlank(message = "Court name is required")
        String name,

        @NotNull(message = "Sport is required")
        Long sportId,

        @NotNull(message = "Price per slot is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Price per slot must not be negative")
        BigDecimal pricePerSlot
) {
}
