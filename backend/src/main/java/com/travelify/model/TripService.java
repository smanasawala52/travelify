package com.travelify.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Add-on linkage between an {@link AgentTrip} and a provider {@link Service}.
 */
@Entity
@Table(
        name = "trip_services",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_trip_services_trip_service", columnNames = {"agent_trip_id", "service_id"})
        },
        indexes = {
                @Index(name = "idx_trip_services_trip", columnList = "agent_trip_id"),
                @Index(name = "idx_trip_services_service", columnList = "service_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agent_trip_id", nullable = false)
    private AgentTrip agentTrip;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @Column(name = "is_optional", nullable = false)
    @Builder.Default
    private Boolean isOptional = true;

    /** Agent-specific price override for this add-on on this trip. */
    @Column(name = "override_price", precision = 10, scale = 2)
    private BigDecimal overridePrice;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (isOptional == null) {
            isOptional = true;
        }
    }
}
