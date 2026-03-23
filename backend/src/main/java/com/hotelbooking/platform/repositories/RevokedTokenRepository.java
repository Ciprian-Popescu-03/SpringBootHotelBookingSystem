package com.hotelbooking.platform.repositories;

import com.hotelbooking.platform.entities.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {
    boolean existsByJti(String jti);
    boolean existsByJtiAndExpiresAtAfter(String jti, Instant instant);
}
