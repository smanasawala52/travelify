package com.travelify.repository;

import com.travelify.model.Payment;
import com.travelify.model.Booking; // Import Booking for Payment.PaymentStatus
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByTransactionId(String transactionId);
    Optional<Payment> findTopByBookingIdAndStatusOrderByPaidAtDesc(Long bookingId, Payment.PaymentStatus status);
}
