package com.fifa.fifarest.service;

import com.fifa.fifarest.domain.RefreshToken;
import com.fifa.fifarest.domain.User;
import com.fifa.fifarest.dto.AuthResponse;
import com.fifa.fifarest.dto.LoginRequest;
import com.fifa.fifarest.dto.RegisterRequest;
import com.fifa.fifarest.dto.UserResponse;
import com.fifa.fifarest.exception.EmailAlreadyExistsException;
import com.fifa.fifarest.exception.InvalidRefreshTokenException;
import com.fifa.fifarest.repository.RefreshTokenRepository;
import com.fifa.fifarest.repository.UserRepository;
import com.fifa.fifarest.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final long refreshTokenExpirationMs;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(UserRepository userRepository,
                        RefreshTokenRepository refreshTokenRepository,
                        PasswordEncoder passwordEncoder,
                        AuthenticationManager authenticationManager,
                        JwtService jwtService,
                        @Value("${app.jwt.refresh-token-expiration-ms}") long refreshTokenExpirationMs) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .phone(request.phone())
                .role(request.roleAsEnum())
                .enabled(true)
                .build();

        user = userRepository.save(user);

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid email or password"));

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse refresh(String refreshTokenValue) {
        RefreshToken existing = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token is invalid"));

        if (existing.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(existing);
            throw new InvalidRefreshTokenException("Refresh token has expired, please log in again");
        }

        User user = existing.getUser();
        refreshTokenRepository.delete(existing);

        return buildAuthResponse(user);
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenRepository.findByToken(refreshTokenValue)
                .ifPresent(refreshTokenRepository::delete);
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshTokenValue = generateSecureToken();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .user(user)
                .expiryDate(Instant.now().plus(refreshTokenExpirationMs, ChronoUnit.MILLIS))
                .build();
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.of(accessToken, refreshTokenValue, jwtService.getAccessTokenExpirationMs(), UserResponse.from(user));
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[64];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
