package com.travelify.dto;

import com.travelify.model.PublishStatus;
import com.travelify.model.ServiceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public final class TripServiceLinkDtos {
    private TripServiceLinkDtos() {}

    @Data
    public static class AddServiceToTripRequest {
        @NotNull
        private Long serviceId;

        private Boolean isOptional = true;

        @DecimalMin("0.0")
        private BigDecimal overridePrice;
    }

    @Data
    public static class UpdateTripServiceRequest {
        private Boolean isOptional;

        @DecimalMin("0.0")
        private BigDecimal overridePrice;

        /** When true, clears any agent price override (use service base price). */
        private Boolean clearOverridePrice;
    }

    @Data
    @Builder
    public static class TripServiceResponse {
        private Long id;
        private Long agentTripId;
        private Long serviceId;
        private String serviceName;
        private ServiceType serviceType;
        private String serviceDescription;
        private BigDecimal servicePrice;
        private String serviceCurrency;
        @Builder.Default
        private Map<String, Object> serviceMeta = new HashMap<>();
        private PublishStatus serviceStatus;
        private Long providerId;
        private String providerEmail;
        private Boolean isOptional;
        private BigDecimal overridePrice;
        /** Effective price: override if set, otherwise service base price. */
        private BigDecimal effectivePrice;
        private Instant createdAt;
    }
}
