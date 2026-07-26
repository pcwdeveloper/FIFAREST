package com.fifa.fifarest.repository;

import com.fifa.fifarest.domain.RefreshToken;
import com.fifa.fifarest.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);
}
