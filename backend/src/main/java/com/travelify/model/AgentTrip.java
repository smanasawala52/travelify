package com.travelify.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(
        name = "agent_trips",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_agent_trips_agent_slug", columnNames = {"agent_id", "slug"})
        },
        indexes = {
                @Index(name = "idx_agent_trips_agent", columnList = "agent_id"),
                @Index(name = "idx_agent_trips_status", columnList = "status"),
                @Index(name = "idx_agent_trips_agent_status", columnList = "agent_id,status"),
                @Index(name = "idx_agent_trips_template", columnList = "template_id"),
                @Index(name = "idx_agent_trips_category", columnList = "category_id"),
                @Index(name = "idx_agent_trips_featured", columnList = "is_featured")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentTrip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private TripTemplate template;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agent_id", nullable = false)
    private User agent;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 255)
    private String slug;

    @Column(name = "short_description", columnDefinition = "TEXT")
    private String shortDescription;

    @Column(name = "full_description", columnDefinition = "TEXT")
    private String fullDescription;

    @Column(name = "featured_image", length = 500)
    private String featuredImage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private TripCategory category;

    @Column(length = 50)
    private String difficulty;

    @Column(name = "duration_days")
    private Integer durationDays;

    @Column(name = "min_age")
    private Integer minAge;

    @Column(name = "max_group_size")
    private Integer maxGroupSize;

    @Column(name = "is_featured", nullable = false)
    @Builder.Default
    private Boolean isFeatured = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PublishStatus status = PublishStatus.DRAFT;

    /**
     * Which fields are overridden relative to the source template
     * (e.g. {@code {"title": true, "price": true}}).
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "override_fields")
    @Builder.Default
    private Map<String, Boolean> overrideFields = new HashMap<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "agentTrip", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dayNumber ASC")
    @Builder.Default
    private List<AgentTripItinerary> itinerary = new ArrayList<>();

    @OneToMany(mappedBy = "agentTrip", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AgentTripPricing> pricing = new ArrayList<>();

    @OneToMany(mappedBy = "agentTrip", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("departureDate ASC")
    @Builder.Default
    private List<AgentTripDeparture> departures = new ArrayList<>();

    @OneToMany(mappedBy = "agentTrip", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<AgentTripImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "agentTrip", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TripService> tripServices = new ArrayList<>();

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        if (isFeatured == null) {
            isFeatured = false;
        }
        if (status == null) {
            status = PublishStatus.DRAFT;
        }
        if (overrideFields == null) {
            overrideFields = new HashMap<>();
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public void addItineraryDay(AgentTripItinerary day) {
        itinerary.add(day);
        day.setAgentTrip(this);
    }

    public void addPricing(AgentTripPricing row) {
        pricing.add(row);
        row.setAgentTrip(this);
    }

    public void addDeparture(AgentTripDeparture departure) {
        departures.add(departure);
        departure.setAgentTrip(this);
    }

    public void addImage(AgentTripImage image) {
        images.add(image);
        image.setAgentTrip(this);
    }

    public void addTripService(TripService tripService) {
        tripServices.add(tripService);
        tripService.setAgentTrip(this);
    }
}
