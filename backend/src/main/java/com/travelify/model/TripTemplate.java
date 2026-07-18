package com.travelify.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "trip_templates",
        indexes = {
                @Index(name = "idx_trip_templates_status", columnList = "status"),
                @Index(name = "idx_trip_templates_category", columnList = "category_id"),
                @Index(name = "idx_trip_templates_featured", columnList = "is_featured"),
                @Index(name = "idx_trip_templates_created_by", columnList = "created_by")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, unique = true, length = 255)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

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
        if (isFeatured == null) {
            isFeatured = false;
        }
        if (status == null) {
            status = PublishStatus.DRAFT;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
