package com.fifa.fifarest.web;

import com.fifa.fifarest.dto.AuthResponse;
import com.fifa.fifarest.dto.LoginRequest;
import com.fifa.fifarest.dto.RefreshRequest;
import com.fifa.fifarest.dto.RegisterRequest;
import com.fifa.fifarest.dto.UserResponse;
import com.fifa.fifarest.service.AuthService;
import com.fifa.fifarest.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        UserResponse response = UserResponse.from(userService.getByEmail(authentication.getName()));
        return ResponseEntity.ok(response);
    }
}
