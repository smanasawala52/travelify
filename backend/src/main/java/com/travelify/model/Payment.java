package com.travelify.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.databind.JsonNode;
import com.travelify.converter.JsonNodeConverter; // Import the converter

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", length = 3)
    private String currency;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Convert(converter = JsonNodeConverter.class) // Apply the converter
    @Column(name = "gateway_response", columnDefinition = "TEXT")
    private JsonNode gatewayResponse;

    @Column(name = "paid_at")
    private Instant paidAt;

    public enum PaymentStatus {
        PENDING, SUCCESS, FAILED, REFUNDED
    }
}