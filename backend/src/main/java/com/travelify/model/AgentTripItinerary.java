package com.travelify.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "agent_trip_itinerary",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_agent_trip_itinerary_day", columnNames = {"agent_trip_id", "day_number"})
        },
        indexes = {
                @Index(name = "idx_agent_trip_itinerary_trip", columnList = "agent_trip_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentTripItinerary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agent_trip_id", nullable = false)
    private AgentTrip agentTrip;

    @Column(name = "day_number", nullable = false)
    private Integer dayNumber;

    @Column(length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String activities;

    @Column(length = 255)
    private String accommodation;

    @Column(length = 255)
    private String meals;
}
