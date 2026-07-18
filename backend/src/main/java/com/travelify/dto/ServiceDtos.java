package com.travelify.dto;

import com.travelify.model.PublishStatus;
import com.travelify.model.ServiceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public final class ServiceDtos {
    private ServiceDtos() {}

    @Data
    public static class ServiceRequest {
        @NotNull
        private ServiceType serviceType;

        @NotBlank
        @Size(max = 255)
        private String name;

        private String description;

        @NotNull
        @DecimalMin("0.0")
        private BigDecimal price;

        @Size(min = 3, max = 3)
        private String currency = "USD";

        private Map<String, Object> meta = new HashMap<>();

        private PublishStatus status;
    }

    @Data
    @Builder
    public static class ServiceResponse {
        private Long id;
        private Long providerId;
        private String providerEmail;
        private String providerBusinessName;
        private ServiceType serviceType;
        private String name;
        private String description;
        private BigDecimal price;
        private String currency;
        @Builder.Default
        private Map<String, Object> meta = new HashMap<>();
        private PublishStatus status;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
