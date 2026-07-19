package com.travelify.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "booking_addons")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingAddon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_service_id", nullable = false)
    private TripService tripService;

    private Integer quantity = 1;

    @Column(name = "price_at_booking", precision = 10, scale = 2)
    private BigDecimal priceAtBooking;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();
}
