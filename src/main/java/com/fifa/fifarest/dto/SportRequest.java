package com.fifa.fifarest.dto;

import jakarta.validation.constraints.NotBlank;

public record SportRequest(
        @NotBlank(message = "Sport name is required")
        String name
) {
}
