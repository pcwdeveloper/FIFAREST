package com.fifa.fifarest.dto;

import jakarta.validation.constraints.NotBlank;

public record VenueRequest(
        @NotBlank(message = "Venue name is required")
        String name,

        @NotBlank(message = "Address is required")
        String address,

        @NotBlank(message = "City is required")
        String city,

        String description
) {
}
