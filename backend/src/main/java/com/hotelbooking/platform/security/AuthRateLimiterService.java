package com.hotelbooking.platform.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class AuthRateLimiterService {
    private final ConcurrentMap<String, WindowCounter> counters = new ConcurrentHashMap<>();
    private final int maxAttempts;
    private final Duration window;

    public AuthRateLimiterService(
            @Value("${application.security.rate-limit.auth.max-attempts:10}") int maxAttempts,
            @Value("${application.security.rate-limit.auth.window-seconds:60}") long windowSeconds
    ) {
        this.maxAttempts = maxAttempts;
        this.window = Duration.ofSeconds(windowSeconds);
    }

    public boolean allow(String key) {
        WindowCounter counter = counters.computeIfAbsent(key, ignored -> new WindowCounter(Instant.now(), 0));
        synchronized (counter) {
            Instant now = Instant.now();
            if (counter.windowStart.plus(window).isBefore(now)) {
                counter.windowStart = now;
                counter.attempts = 0;
            }
            counter.attempts++;
            return counter.attempts <= maxAttempts;
        }
    }

    private static final class WindowCounter {
        private Instant windowStart;
        private int attempts;

        private WindowCounter(Instant windowStart, int attempts) {
            this.windowStart = windowStart;
            this.attempts = attempts;
        }
    }
}
