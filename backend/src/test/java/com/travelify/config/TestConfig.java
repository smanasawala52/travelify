package com.travelify.config;

import com.travelify.security.JwtProperties;
import com.travelify.security.JwtUtil;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Test configuration for integration tests.
 */
@TestConfiguration
public class TestConfig {

    @Bean
    public JwtProperties jwtProperties() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("test-secret-key-at-least-256-bits-long-12345678901234567890123456789012");
        properties.setAccessExpirationMs(86400000L);
        properties.setRefreshExpirationMs(604800000L);
        return properties;
    }

    @Bean
    public JwtUtil jwtUtil(JwtProperties jwtProperties) {
        return new JwtUtil(jwtProperties);
    }
}
