package com.rober.bookshop.service.impl;

import com.rober.bookshop.service.IRateLimitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class RateLimitService implements IRateLimitService {

    @Value("${app.rate-limit.auth.max-attempts:5}")
    private int maxAttempts;

    @Value("${app.rate-limit.auth.window-minutes:15}")
    private int windowMinutes;

    // Thread-safe in-memory storage for rate limiting
    private final Map<String, RateLimitEntry> rateLimitStore = new ConcurrentHashMap<>();

    @Override
    public boolean isAllowed(String clientIp, String endpoint) {
        String key = generateKey(clientIp, endpoint);
        RateLimitEntry entry = rateLimitStore.get(key);
        
        LocalDateTime now = LocalDateTime.now();
        
        if (entry == null) {
            // First request from this IP for this endpoint
            entry = new RateLimitEntry(1, now);
            rateLimitStore.put(key, entry);
            log.debug("First request from IP {} for endpoint {}", clientIp, endpoint);
            return true;
        }
        
        // Check if window has expired
        if (ChronoUnit.MINUTES.between(entry.getWindowStart(), now) >= windowMinutes) {
            // Reset the window
            entry.setAttempts(1);
            entry.setWindowStart(now);
            log.debug("Rate limit window reset for IP {} endpoint {}", clientIp, endpoint);
            return true;
        }
        
        // Check if within rate limit
        if (entry.getAttempts() < maxAttempts) {
            entry.incrementAttempts();
            return true;
        }
        
        // Rate limit exceeded
        log.warn("Rate limit exceeded for IP {} on endpoint {}. Attempts: {}", 
                clientIp, endpoint, entry.getAttempts());
        return false;
    }

    @Override
    public int getRemainingAttempts(String clientIp, String endpoint) {
        String key = generateKey(clientIp, endpoint);
        RateLimitEntry entry = rateLimitStore.get(key);
        
        if (entry == null) {
            return maxAttempts;
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (ChronoUnit.MINUTES.between(entry.getWindowStart(), now) >= windowMinutes) {
            return maxAttempts;
        }
        
        return Math.max(0, maxAttempts - entry.getAttempts());
    }

    @Override
    public long getTimeUntilReset(String clientIp, String endpoint) {
        String key = generateKey(clientIp, endpoint);
        RateLimitEntry entry = rateLimitStore.get(key);
        
        if (entry == null) {
            return 0;
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime resetTime = entry.getWindowStart().plusMinutes(windowMinutes);
        
        return Math.max(0, ChronoUnit.SECONDS.between(now, resetTime));
    }

    private String generateKey(String clientIp, String endpoint) {
        return clientIp + ":" + endpoint;
    }

    private static class RateLimitEntry {
        private int attempts;
        private LocalDateTime windowStart;

        public RateLimitEntry(int attempts, LocalDateTime windowStart) {
            this.attempts = attempts;
            this.windowStart = windowStart;
        }

        public int getAttempts() {
            return attempts;
        }

        public void setAttempts(int attempts) {
            this.attempts = attempts;
        }

        public void incrementAttempts() {
            this.attempts++;
        }

        public LocalDateTime getWindowStart() {
            return windowStart;
        }

        public void setWindowStart(LocalDateTime windowStart) {
            this.windowStart = windowStart;
        }
    }
}
