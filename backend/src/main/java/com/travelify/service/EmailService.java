package com.travelify.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// This is a placeholder for a real email service integration (e.g., SendGrid, JavaMailSender)
@Service
@RequiredArgsConstructor
public class EmailService {

    public void sendEmail(String to, String subject, String body) {
        // In a real application, this would integrate with an actual email sending library
        // For now, we'll just print to console or log.
        System.out.println("Sending email to: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("Body:\n" + body);
        System.out.println("--- End Email ---");
    }

    public void sendPasswordResetEmail(String email, String resetLink) {
        String subject = "Password Reset Request for Travelify Account";
        String body = String.format(
                "Dear User,\n\n" +
                        "You have requested to reset the password for your Travelify account. " +
                        "Please click on the following link to reset your password:\n\n" +
                        "%s\n\n" +
                        "This link will expire in 1 hour. If you did not request a password reset, please ignore this email.\n\n" +
                        "Thank you,\n" +
                        "Travelify Team",
                resetLink
        );
        sendEmail(email, subject, body);
    }
}
