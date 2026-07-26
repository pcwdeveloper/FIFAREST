package com.fifa.fifarest.dto;

import com.fifa.fifarest.domain.Role;
import com.fifa.fifarest.domain.User;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        Role role
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getPhone(), user.getRole());
    }
}
