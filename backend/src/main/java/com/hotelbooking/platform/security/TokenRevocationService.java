package com.hotelbooking.platform.security;

import com.hotelbooking.platform.entities.RevokedToken;
import com.hotelbooking.platform.repositories.RevokedTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenRevocationService {
    private final RevokedTokenRepository revokedTokenRepository;

    @Transactional
    public void revoke(String jti, Instant expiresAt) {
        if (jti == null || expiresAt == null || expiresAt.isBefore(Instant.now())) {
            return;
        }
        if (revokedTokenRepository.existsByJti(jti)) {
            return;
        }
        RevokedToken revokedToken = RevokedToken.builder()
                .jti(jti)
                .revokedAt(Instant.now())
                .expiresAt(expiresAt)
                .build();
        revokedTokenRepository.save(revokedToken);
    }

    @Transactional(readOnly = true)
    public boolean isRevoked(String jti) {
        if (jti == null) {
            return false;
        }
        return revokedTokenRepository.existsByJtiAndExpiresAtAfter(jti, Instant.now());
    }
}
