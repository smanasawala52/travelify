package com.travelify.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_reference", unique = true, nullable = false, length = 20)
    private String bookingReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_trip_id", nullable = false)
    private AgentTrip agentTrip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departure_id", nullable = false)
    private AgentTripDeparture departure;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "currency", length = 3)
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private BookingStatus status = BookingStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 20)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    private Integer adults = 1;
    private Integer children = 0;
    private Integer infants = 0;

    @Column(name = "special_requests", columnDefinition = "TEXT")
    private String specialRequests;

    @Column(name = "coupon_code", length = 50)
    private String couponCode;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "booked_at")
    private Instant bookedAt = Instant.now();

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "payment_due_date")
    private Instant paymentDueDate;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookingCustomer> bookingCustomers;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookingAddon> bookingAddons;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookingStatusHistory> statusHistory;

    public enum BookingStatus {
        PENDING, PAID, CONFIRMED, CANCELLED, COMPLETED
    }

    public enum PaymentStatus {
        PENDING, PAID, REFUNDED
    }
}
