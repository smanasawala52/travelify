package com.travelify.security;

import com.travelify.exception.InvalidTokenException;
import com.travelify.exception.TokenExpiredException;
import com.travelify.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * JWT helpers for Travelify — modern, stateless alternative to WP Travel application passwords.
 */
@Component
public class JwtUtil {

    public static final String CLAIM_TYPE = "type";
    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_UID = "uid";
    public static final String CLAIM_JTI = "jti";

    private final JwtProperties properties;

    public JwtUtil(JwtProperties properties) {
        this.properties = properties;
    }

    public String generateAccessToken(User user) {
        return buildToken(user, TYPE_ACCESS, properties.getAccessExpirationMs());
    }

    public String generateRefreshToken(User user) {
        return buildToken(user, TYPE_REFRESH, properties.getRefreshExpirationMs());
    }

    public boolean isAccessToken(String token) {
        return TYPE_ACCESS.equals(extractClaim(token, c -> c.get(CLAIM_TYPE, String.class)));
    }

    public boolean isRefreshToken(String token) {
        return TYPE_REFRESH.equals(extractClaim(token, c -> c.get(CLAIM_TYPE, String.class)));
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        Number uid = extractClaim(token, c -> c.get(CLAIM_UID, Number.class));
        return uid == null ? null : uid.longValue();
    }

    public String extractRole(String token) {
        return extractClaim(token, c -> c.get(CLAIM_ROLE, String.class));
    }

    public String extractJti(String token) {
        return extractClaim(token, c -> {
            String jti = c.getId();
            if (jti == null) {
                jti = c.get(CLAIM_JTI, String.class);
            }
            return jti;
        });
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return !isTokenExpired(token);
        } catch (TokenExpiredException | InvalidTokenException ex) {
            return false;
        }
    }

    public boolean isTokenValid(String token, String expectedEmail) {
        try {
            String email = extractEmail(token);
            return email != null
                    && email.equalsIgnoreCase(expectedEmail)
                    && !isTokenExpired(token);
        } catch (TokenExpiredException | InvalidTokenException ex) {
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (TokenExpiredException ex) {
            return true;
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(parseClaims(token));
    }

    public Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(signingKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException ex) {
            throw new TokenExpiredException("Token has expired");
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException ex) {
            throw new InvalidTokenException("Invalid token");
        }
    }

    public long getAccessExpirationMs() {
        return properties.getAccessExpirationMs();
    }

    private String buildToken(User user, String type, long ttlMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + ttlMs);
        String jti = UUID.randomUUID().toString();

        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_TYPE, type);
        claims.put(CLAIM_ROLE, user.getRole().name());
        claims.put(CLAIM_UID, user.getId());
        claims.put("name", user.getFullName());
        claims.put("firstName", user.getFirstName());
        claims.put("lastName", user.getLastName());

        return Jwts.builder()
                .setId(jti)
                .setSubject(user.getEmail())
                .addClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key signingKey() {
        byte[] keyBytes = properties.getSecret().getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, Math.min(keyBytes.length, 32));
            keyBytes = padded;
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
