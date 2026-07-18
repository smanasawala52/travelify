package com.travelify.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_email", columnList = "email", unique = true),
                @Index(name = "idx_users_role", columnList = "role")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    /** BCrypt (or other encoder) hashed password — never store plaintext. */
    @Column(nullable = false, length = 255)
    private String password;

    @Column(length = 100)
    private String firstName;

    @Column(length = 100)
    private String lastName;

    @Column(length = 30)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.CUSTOMER;

    /**
     * Optional provider specialization for {@link Role#AGENT} accounts
     * (travel agent, hotel, insurance, visa).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type", length = 30)
    private ProviderType providerType;

    @Column(name = "business_name", length = 255)
    private String businessName;

    @Column(name = "business_address", columnDefinition = "TEXT")
    private String businessAddress;

    @Column(length = 512)
    private String avatarUrl;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Column
    private Instant lastLoginAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        if (role == null) {
            role = Role.CUSTOMER;
        }
        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    @Transient
    public String getFullName() {
        String first = firstName == null ? "" : firstName.trim();
        String last = lastName == null ? "" : lastName.trim();
        String combined = (first + " " + last).trim();
        return combined.isEmpty() ? email : combined;
    }
}
