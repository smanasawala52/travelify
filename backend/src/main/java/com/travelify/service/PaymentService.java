package com.travelify.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelify.dto.BookingDtos;
import com.travelify.exception.PaymentFailedException;
import com.travelify.model.Booking;
import com.travelify.model.Payment;
import com.travelify.repository.BookingRepository;
import com.travelify.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper; // For handling JSONB gateway_response

    @Transactional
    public BookingDtos.PaymentInitiationResponse initiatePayment(Long bookingId, String paymentMethod) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new PaymentFailedException("Booking not found."));

        if (booking.getPaymentStatus() == Booking.PaymentStatus.PAID) {
            throw new PaymentFailedException("Booking already paid.");
        }

        // Create a new payment record in PENDING status
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalPrice());
        payment.setCurrency(booking.getCurrency());
        payment.setPaymentMethod(paymentMethod);
        payment.setStatus(Payment.PaymentStatus.PENDING);
        paymentRepository.save(payment);

        // Placeholder for actual payment gateway integration
        // In a real application, this would call Stripe/PayPal SDK
        // and return a payment intent client secret or a redirect URL.
        String transactionId = "TXN_" + System.currentTimeMillis(); // Mock transaction ID
        String paymentIntentId = "pi_" + System.currentTimeMillis(); // Mock payment intent ID
        String redirectUrl = null; // Mock redirect URL

        // Simulate success for now
        // In a real scenario, this would depend on the gateway response
        payment.setTransactionId(transactionId);
        payment.setGatewayResponse(objectMapper.createObjectNode().put("mockResponse", "Payment initiated successfully"));
        paymentRepository.save(payment);

        // Update booking payment status to PENDING (if not already)
        booking.setPaymentStatus(Booking.PaymentStatus.PENDING);
        bookingRepository.save(booking);

        return new BookingDtos.PaymentInitiationResponse(paymentIntentId, redirectUrl, "PENDING", "Payment initiated. Awaiting confirmation.");
    }

    @Transactional
    public void handleWebhook(String payload, String signature) {
        // In a real application, verify signature first
        // For now, we'll parse a mock payload to update payment status

        try {
            JsonNode webhookEvent = objectMapper.readTree(payload);
            String transactionId = webhookEvent.has("transactionId") ? webhookEvent.get("transactionId").asText() : null;
            String status = webhookEvent.has("status") ? webhookEvent.get("status").asText() : null;
            Long bookingId = webhookEvent.has("bookingId") ? webhookEvent.get("bookingId").asLong() : null;

            if (transactionId == null || status == null || bookingId == null) {
                throw new IllegalArgumentException("Invalid webhook payload.");
            }

            Optional<Payment> optionalPayment = paymentRepository.findByTransactionId(transactionId);
            Payment payment;

            if (optionalPayment.isEmpty()) {
                // This might be a new payment record from webhook if not initiated by our system
                // Or an error if we expect all payments to be initiated by us.
                // For simplicity, let's assume we find it by transactionId.
                // If not found, we might need to create a new payment record or log an error.
                // For now, we'll throw an exception.
                throw new PaymentFailedException("Payment with transaction ID " + transactionId + " not found.");
            } else {
                payment = optionalPayment.get();
            }

            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new PaymentFailedException("Booking not found for webhook."));

            Payment.PaymentStatus newPaymentStatus = Payment.PaymentStatus.valueOf(status.toUpperCase());
            payment.setStatus(newPaymentStatus);
            payment.setGatewayResponse(webhookEvent); // Store full webhook response
            payment.setPaidAt(Instant.now());
            paymentRepository.save(payment);

            if (newPaymentStatus == Payment.PaymentStatus.SUCCESS) {
                booking.setPaymentStatus(Booking.PaymentStatus.PAID);
                booking.setStatus(Booking.BookingStatus.CONFIRMED); // Auto-confirm on successful payment
                booking.setConfirmedAt(Instant.now());
                bookingRepository.save(booking);
                // TODO: Trigger NotificationService to send confirmation email
            } else if (newPaymentStatus == Payment.PaymentStatus.FAILED) {
                booking.setPaymentStatus(Booking.PaymentStatus.PENDING); // Or FAILED, depending on business logic
                bookingRepository.save(booking);
            } else if (newPaymentStatus == Payment.PaymentStatus.REFUNDED) {
                booking.setPaymentStatus(Booking.PaymentStatus.REFUNDED);
                bookingRepository.save(booking);
            }

        } catch (Exception e) {
            throw new PaymentFailedException("Failed to process webhook: " + e.getMessage());
        }
    }

    @Transactional
    public void refundPayment(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new PaymentFailedException("Booking not found."));

        if (booking.getPaymentStatus() != Booking.PaymentStatus.PAID) {
            throw new PaymentFailedException("Cannot refund a booking that is not paid.");
        }

        // Find the latest successful payment for this booking
        Payment latestPayment = paymentRepository.findTopByBookingIdAndStatusOrderByPaidAtDesc(bookingId, Payment.PaymentStatus.SUCCESS)
                .orElseThrow(() -> new PaymentFailedException("No successful payment found for this booking."));

        // Placeholder for actual payment gateway refund call
        // In a real application, this would call Stripe/PayPal SDK
        // and process the refund.
        boolean refundSuccessful = true; // Mock success

        if (refundSuccessful) {
            Payment refundPayment = new Payment();
            refundPayment.setBooking(booking);
            refundPayment.setAmount(latestPayment.getAmount().negate()); // Negative amount for refund
            refundPayment.setCurrency(latestPayment.getCurrency());
            refundPayment.setPaymentMethod(latestPayment.getPaymentMethod());
            refundPayment.setTransactionId("REFUND_" + latestPayment.getTransactionId()); // Mock refund transaction ID
            refundPayment.setStatus(Payment.PaymentStatus.REFUNDED);
            refundPayment.setPaidAt(Instant.now());
            refundPayment.setGatewayResponse(objectMapper.createObjectNode().put("mockResponse", "Refund processed successfully"));
            paymentRepository.save(refundPayment);

            booking.setPaymentStatus(Booking.PaymentStatus.REFUNDED);
            booking.setStatus(Booking.BookingStatus.CANCELLED); // Cancel booking on full refund
            booking.setCancelledAt(Instant.now());
            bookingRepository.save(booking);
            // TODO: Trigger NotificationService to send refund confirmation
        } else {
            throw new PaymentFailedException("Payment gateway failed to process refund.");
        }
    }

    public Payment.PaymentStatus getPaymentStatus(String transactionId) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new PaymentFailedException("Payment with transaction ID " + transactionId + " not found."));
        // In a real scenario, you might query the gateway for the latest status
        // For now, we return the status from our database
        return payment.getStatus();
    }
}
