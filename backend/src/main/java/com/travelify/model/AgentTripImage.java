package com.travelify.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "agent_trip_images",
        indexes = {
                @Index(name = "idx_agent_trip_images_trip", columnList = "agent_trip_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentTripImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agent_trip_id", nullable = false)
    private AgentTrip agentTrip;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "is_featured", nullable = false)
    @Builder.Default
    private Boolean isFeatured = false;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @PrePersist
    void onCreate() {
        if (isFeatured == null) {
            isFeatured = false;
        }
        if (sortOrder == null) {
            sortOrder = 0;
        }
    }
}
