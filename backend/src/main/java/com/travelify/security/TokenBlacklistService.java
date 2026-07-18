package com.travelify.security;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory JWT blacklist for logout. Entries expire with the token TTL.
 * Suitable for single-instance / dev; use Redis (or similar) in multi-node prod.
 */
@Service
public class TokenBlacklistService {

    private final Map<String, Instant> blacklist = new ConcurrentHashMap<>();

    public void blacklist(String jti, Instant expiresAt) {
        if (jti == null || jti.isBlank()) {
            return;
        }
        blacklist.put(jti, expiresAt == null ? Instant.now().plusSeconds(86_400) : expiresAt);
    }

    public boolean isBlacklisted(String jti) {
        if (jti == null || jti.isBlank()) {
            return false;
        }
        Instant expiresAt = blacklist.get(jti);
        if (expiresAt == null) {
            return false;
        }
        if (expiresAt.isBefore(Instant.now())) {
            blacklist.remove(jti);
            return false;
        }
        return true;
    }

    @Scheduled(fixedDelayString = "3600000")
    public void purgeExpired() {
        Instant now = Instant.now();
        blacklist.entrySet().removeIf(e -> e.getValue().isBefore(now));
    }
}
