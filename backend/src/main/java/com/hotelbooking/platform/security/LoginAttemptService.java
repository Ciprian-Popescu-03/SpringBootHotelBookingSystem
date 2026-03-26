package com.hotelbooking.platform.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class LoginAttemptService {

    private final int maxAttempts;
    private final Duration attemptWindow;
    private final Duration lockDuration;
    private final Duration staleEntryTtl;

    private final ConcurrentMap<String, LoginAttempt> attemptsCache = new ConcurrentHashMap<>();

    public LoginAttemptService(
            @Value("${application.security.login-attempt.max-attempts:5}") int maxAttempts,
            @Value("${application.security.login-attempt.attempt-window-seconds:60}") long attemptWindowSeconds,
            @Value("${application.security.login-attempt.lock-duration-seconds:60}") long lockDurationSeconds,
            @Value("${application.security.login-attempt.stale-entry-ttl-seconds:300}") long staleEntryTtlSeconds
    ) {
        this.maxAttempts = maxAttempts;
        this.attemptWindow = Duration.ofSeconds(attemptWindowSeconds);
        this.lockDuration = Duration.ofSeconds(lockDurationSeconds);
        this.staleEntryTtl = Duration.ofSeconds(staleEntryTtlSeconds);
    }

    public void loginSucceeded(String key) {
        attemptsCache.remove(key);
    }

    public void loginFailed(String key) {
        LoginAttempt attempt = attemptsCache.computeIfAbsent(key, k -> new LoginAttempt());
        Instant now = Instant.now();
        attempt.lastUpdatedAt = now;

        if (attempt.firstAttemptAt.plus(attemptWindow).isBefore(now)) {
            attempt.firstAttemptAt = now;
            attempt.attempts = 0;
            attempt.lockedUntil = null;
        }

        attempt.attempts++;

        if (attempt.attempts >= maxAttempts) {
            attempt.lockedUntil = now.plus(lockDuration);
        }
    }

    public boolean isBlocked(String key) {
        LoginAttempt attempt = attemptsCache.get(key);
        if (attempt == null) {
            return false;
        }

        if (attempt.lockedUntil != null) {
            if (attempt.lockedUntil.isBefore(Instant.now())) {
                attemptsCache.remove(key);
                return false;
            }
            attempt.lastUpdatedAt = Instant.now();
            return true;
        }
        if (attempt.lastUpdatedAt.plus(staleEntryTtl).isBefore(Instant.now())) {
            attemptsCache.remove(key);
        }
        return false;
    }

    @Scheduled(fixedDelayString = "${application.security.login-attempt.cleanup-interval-ms:60000}")
    public void evictStaleEntries() {
        Instant now = Instant.now();
        attemptsCache.entrySet().removeIf(entry -> {
            LoginAttempt attempt = entry.getValue();
            if (attempt.lockedUntil != null && attempt.lockedUntil.isAfter(now)) {
                return false;
            }
            return attempt.lastUpdatedAt.plus(staleEntryTtl).isBefore(now);
        });
    }

    private static class LoginAttempt {
        private int attempts = 0;
        private Instant firstAttemptAt = Instant.now();
        private Instant lastUpdatedAt = Instant.now();
        private Instant lockedUntil = null;
    }
}
