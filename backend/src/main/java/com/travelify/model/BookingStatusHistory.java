package com.travelify.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "booking_status_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "status_from", length = 20)
    private String statusFrom;

    @Column(name = "status_to", length = 20)
    private String statusTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by", nullable = false)
    private User changedBy;

    @Column(name = "changed_at")
    private Instant changedAt = Instant.now();

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;
}
