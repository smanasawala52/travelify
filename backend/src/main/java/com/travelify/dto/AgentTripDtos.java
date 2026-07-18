package com.travelify.dto;

import com.travelify.model.PricingType;
import com.travelify.model.PublishStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AgentTripDtos {
    private AgentTripDtos() {}

    /**
     * Full create/update payload for a custom agent trip (or full replacement of children).
     */
    @Data
    public static class AgentTripRequest {
        @NotBlank
        @Size(max = 255)
        private String title;

        @Size(max = 255)
        private String slug;

        private String shortDescription;
        private String fullDescription;

        @Size(max = 500)
        private String featuredImage;

        private Long categoryId;

        @Size(max = 50)
        private String difficulty;

        @Min(1)
        private Integer durationDays;

        @Min(0)
        private Integer minAge;

        @Min(1)
        private Integer maxGroupSize;

        private Boolean isFeatured;
        private PublishStatus status;

        @Valid
        private List<ItineraryDayRequest> itinerary = new ArrayList<>();

        @Valid
        private List<PricingRequest> pricing = new ArrayList<>();

        @Valid
        private List<DepartureRequest> departures = new ArrayList<>();

        @Valid
        private List<ImageRequest> images = new ArrayList<>();
    }

    /**
     * Partial overrides when creating a trip from a template.
     * Only non-null fields are applied and recorded in {@code override_fields}.
     */
    @Data
    public static class OverrideRequest {
        @Size(max = 255)
        private String title;

        @Size(max = 255)
        private String slug;

        private String shortDescription;
        private String fullDescription;

        @Size(max = 500)
        private String featuredImage;

        private Long categoryId;

        @Size(max = 50)
        private String difficulty;

        @Min(1)
        private Integer durationDays;

        @Min(0)
        private Integer minAge;

        @Min(1)
        private Integer maxGroupSize;

        private Boolean isFeatured;
        private PublishStatus status;

        @Valid
        private List<ItineraryDayRequest> itinerary;

        @Valid
        private List<PricingRequest> pricing;

        @Valid
        private List<DepartureRequest> departures;

        @Valid
        private List<ImageRequest> images;
    }

    @Data
    public static class ItineraryDayRequest {
        @NotNull
        @Min(1)
        private Integer dayNumber;

        @Size(max = 255)
        private String title;
        private String description;
        private String activities;

        @Size(max = 255)
        private String accommodation;

        @Size(max = 255)
        private String meals;
    }

    @Data
    public static class PricingRequest {
        @NotNull
        private PricingType pricingType;

        @DecimalMin("0.0")
        private BigDecimal price;

        @Size(min = 3, max = 3)
        private String currency = "USD";

        @DecimalMin("0.0")
        private BigDecimal adultPrice;

        @DecimalMin("0.0")
        private BigDecimal childPrice;

        @DecimalMin("0.0")
        private BigDecimal infantPrice;

        @Min(1)
        private Integer minParticipants;

        @Min(1)
        private Integer maxParticipants;
    }

    @Data
    public static class DepartureRequest {
        @NotNull
        private LocalDate departureDate;
        private LocalDate endDate;

        @Min(0)
        private Integer availableSeats;

        @DecimalMin("0.0")
        private BigDecimal priceOverride;

        private Boolean isCancelled = false;
    }

    @Data
    public static class ImageRequest {
        @NotBlank
        @Size(max = 500)
        private String imageUrl;
        private Boolean isFeatured = false;
        private Integer sortOrder = 0;
    }

    @Data
    @Builder
    public static class AgentTripResponse {
        private Long id;
        private Long templateId;
        private String templateTitle;
        private Long agentId;
        private String agentEmail;
        private String title;
        private String slug;
        private String shortDescription;
        private String fullDescription;
        private String featuredImage;
        private Long categoryId;
        private String categoryName;
        private String difficulty;
        private Integer durationDays;
        private Integer minAge;
        private Integer maxGroupSize;
        private Boolean isFeatured;
        private PublishStatus status;
        @Builder.Default
        private Map<String, Boolean> overrideFields = new HashMap<>();
        /** True when response values were merged from a linked template. */
        private Boolean mergedFromTemplate;
        private Instant createdAt;
        private Instant updatedAt;
        @Builder.Default
        private List<ItineraryDayResponse> itinerary = new ArrayList<>();
        @Builder.Default
        private List<PricingResponse> pricing = new ArrayList<>();
        @Builder.Default
        private List<DepartureResponse> departures = new ArrayList<>();
        @Builder.Default
        private List<ImageResponse> images = new ArrayList<>();
    }

    @Data
    @Builder
    public static class ItineraryDayResponse {
        private Long id;
        private Integer dayNumber;
        private String title;
        private String description;
        private String activities;
        private String accommodation;
        private String meals;
    }

    @Data
    @Builder
    public static class PricingResponse {
        private Long id;
        private PricingType pricingType;
        private BigDecimal price;
        private String currency;
        private BigDecimal adultPrice;
        private BigDecimal childPrice;
        private BigDecimal infantPrice;
        private Integer minParticipants;
        private Integer maxParticipants;
    }

    @Data
    @Builder
    public static class DepartureResponse {
        private Long id;
        private LocalDate departureDate;
        private LocalDate endDate;
        private Integer availableSeats;
        private BigDecimal priceOverride;
        private Boolean isCancelled;
    }

    @Data
    @Builder
    public static class ImageResponse {
        private Long id;
        private String imageUrl;
        private Boolean isFeatured;
        private Integer sortOrder;
    }
}
