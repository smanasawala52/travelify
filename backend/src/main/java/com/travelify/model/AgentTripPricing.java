package com.travelify.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "agent_trip_pricing",
        indexes = {
                @Index(name = "idx_agent_trip_pricing_trip", columnList = "agent_trip_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentTripPricing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agent_trip_id", nullable = false)
    private AgentTrip agentTrip;

    @Enumerated(EnumType.STRING)
    @Column(name = "pricing_type", nullable = false, length = 20)
    private PricingType pricingType;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "USD";

    @Column(name = "adult_price", precision = 10, scale = 2)
    private BigDecimal adultPrice;

    @Column(name = "child_price", precision = 10, scale = 2)
    private BigDecimal childPrice;

    @Column(name = "infant_price", precision = 10, scale = 2)
    private BigDecimal infantPrice;

    @Column(name = "min_participants")
    private Integer minParticipants;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @PrePersist
    void onCreate() {
        if (currency == null || currency.isBlank()) {
            currency = "USD";
        }
    }
}
