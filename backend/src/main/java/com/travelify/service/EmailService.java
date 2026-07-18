package com.travelify.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Placeholder email sender — logs reset links until SMTP is configured.
 */
@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        // TODO: integrate SMTP / SendGrid / SES
        log.info("Password reset email (placeholder) → to={} token={}", toEmail, resetToken);
        log.info("Reset link (dev): http://localhost:5173/reset-password?token={}", resetToken);
    }
}
