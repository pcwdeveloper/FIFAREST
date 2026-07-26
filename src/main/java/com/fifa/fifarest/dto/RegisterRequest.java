package com.fifa.fifarest.dto;

import com.fifa.fifarest.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Full name is required")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password,

        String phone,

        @NotBlank(message = "Role is required")
        @Pattern(regexp = "PLAYER|VENUE_OWNER", message = "Role must be PLAYER or VENUE_OWNER")
        String role
) {
    public Role roleAsEnum() {
        return Role.valueOf(role);
    }
}
