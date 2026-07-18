package com.travelify.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "travelify.jwt")
public class JwtProperties {
    private String secret;
    /** Access token TTL — default 24 hours. */
    private long accessExpirationMs = 86_400_000L;
    /** Refresh token TTL — default 7 days. */
    private long refreshExpirationMs = 604_800_000L;

    /**
     * Backward-compatible alias for {@link #accessExpirationMs}
     * when only {@code travelify.jwt.expiration-ms} is set.
     */
    public void setExpirationMs(long expirationMs) {
        this.accessExpirationMs = expirationMs;
    }
}
