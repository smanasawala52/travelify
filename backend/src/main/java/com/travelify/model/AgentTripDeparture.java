package com.travelify.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "agent_trip_departures",
        indexes = {
                @Index(name = "idx_agent_trip_departures_trip", columnList = "agent_trip_id"),
                @Index(name = "idx_agent_trip_departures_date", columnList = "departure_date"),
                @Index(name = "idx_agent_trip_departures_trip_date", columnList = "agent_trip_id,departure_date")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentTripDeparture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agent_trip_id", nullable = false)
    private AgentTrip agentTrip;

    @Column(name = "departure_date", nullable = false)
    private LocalDate departureDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "available_seats")
    private Integer availableSeats;

    @Column(name = "price_override", precision = 10, scale = 2)
    private BigDecimal priceOverride;

    @Column(name = "is_cancelled", nullable = false)
    @Builder.Default
    private Boolean isCancelled = false;

    @PrePersist
    void onCreate() {
        if (isCancelled == null) {
            isCancelled = false;
        }
    }
}
