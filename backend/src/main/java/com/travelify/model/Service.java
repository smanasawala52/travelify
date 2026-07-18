package com.travelify.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Provider-managed offering (hotel room, insurance plan, visa service, or custom)
 * that agents can attach to trips as optional add-ons.
 */
@Entity
@Table(
        name = "services",
        indexes = {
                @Index(name = "idx_services_provider", columnList = "provider_id"),
                @Index(name = "idx_services_type", columnList = "service_type"),
                @Index(name = "idx_services_status", columnList = "status"),
                @Index(name = "idx_services_provider_type", columnList = "provider_id,service_type"),
                @Index(name = "idx_services_provider_status", columnList = "provider_id,status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private User provider;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 30)
    private ServiceType serviceType;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "USD";

    /**
     * Type-specific metadata (amenities, coverage, visa country, etc.).
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "meta")
    @Builder.Default
    private Map<String, Object> meta = new HashMap<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PublishStatus status = PublishStatus.DRAFT;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        if (currency == null || currency.isBlank()) {
            currency = "USD";
        }
        if (status == null) {
            status = PublishStatus.DRAFT;
        }
        if (meta == null) {
            meta = new HashMap<>();
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
