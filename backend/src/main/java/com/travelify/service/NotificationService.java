package com.travelify.service;

import com.travelify.model.Booking;
import com.travelify.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final BookingRepository bookingRepository;
    private final EmailService emailService; // Assuming an EmailService exists

    public void sendBookingConfirmation(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found for confirmation email."));

        String subject = "Travelify Booking Confirmation: " + booking.getBookingReference();
        String body = String.format(
                "Dear %s,\n\n" +
                        "Your booking with reference %s for %s has been successfully created.\n" +
                        "Total Price: %s %s\n" +
                        "Status: %s\n\n" +
                        "Please complete your payment by %s to confirm your trip.\n\n" +
                        "Thank you for booking with Travelify!",
                booking.getUser().getFullName(),
                booking.getBookingReference(),
                booking.getAgentTrip().getTitle(),
                booking.getTotalPrice(),
                booking.getCurrency(),
                booking.getStatus(),
                booking.getPaymentDueDate()
        );
        emailService.sendEmail(booking.getUser().getEmail(), subject, body);
    }

    public void sendPaymentReceipt(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found for payment receipt email."));

        String subject = "Travelify Payment Receipt for Booking: " + booking.getBookingReference();
        String body = String.format(
                "Dear %s,\n\n" +
                        "This is a receipt for your payment for booking reference %s.\n" +
                        "Amount Paid: %s %s\n" +
                        "Payment Status: %s\n\n" +
                        "Your trip %s is now confirmed.\n\n" +
                        "Thank you for booking with Travelify!",
                booking.getUser().getFullName(),
                booking.getBookingReference(),
                booking.getTotalPrice(), // Assuming full payment
                booking.getCurrency(),
                booking.getPaymentStatus(),
                booking.getAgentTrip().getTitle()
        );
        // In a real application, you might attach an invoice PDF here
        emailService.sendEmail(booking.getUser().getEmail(), subject, body);
    }

    public void sendBookingStatusUpdate(Long bookingId, Booking.BookingStatus newStatus) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found for status update email."));

        String subject = "Travelify Booking Status Update: " + booking.getBookingReference();
        String body = String.format(
                "Dear %s,\n\n" +
                        "The status of your booking with reference %s for %s has been updated to: %s.\n\n" +
                        "If you have any questions, please contact us.\n\n" +
                        "Thank you for booking with Travelify!",
                booking.getUser().getFullName(),
                booking.getBookingReference(),
                booking.getAgentTrip().getTitle(),
                newStatus.name()
        );
        emailService.sendEmail(booking.getUser().getEmail(), subject, body);
        // Also send to agent if necessary
        emailService.sendEmail(booking.getAgentTrip().getAgent().getEmail(), "Booking Status Update for Your Trip: " + booking.getBookingReference(), body);
    }

    public void sendBookingReminder(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found for reminder email."));

        String subject = "Travelify Booking Reminder: " + booking.getBookingReference();
        String body = String.format(
                "Dear %s,\n\n" +
                        "This is a friendly reminder about your upcoming trip %s with booking reference %s.\n" +
                        "Departure Date: %s\n\n" +
                        "Please ensure all details are correct and contact us if you have any questions.\n\n" +
                        "We look forward to seeing you!",
                booking.getUser().getFullName(),
                booking.getAgentTrip().getTitle(),
                booking.getBookingReference(),
                booking.getDeparture().getDepartureDate()
        );
        emailService.sendEmail(booking.getUser().getEmail(), subject, body);
    }
}
