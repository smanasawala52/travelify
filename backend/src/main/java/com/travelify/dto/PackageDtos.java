package com.travelify.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

public final class PackageDtos {
    private PackageDtos() {}

    @Data
    public static class PackageRequest {
        @NotBlank
        private String title;
        private String description;
        @NotBlank
        private String destination;
        @NotNull @DecimalMin("0.0")
        private BigDecimal price;
        @NotNull @Min(1)
        private Integer durationDays;
        private Boolean active = true;
    }

    @Data
    @Builder
    public static class PackageResponse {
        private Long id;
        private String title;
        private String description;
        private String destination;
        private BigDecimal price;
        private Integer durationDays;
        private Boolean active;
        private String categoryName; // Added category name
        private String categorySlug; // Added category slug
    }

    @Data
    public static class PackageFilterRequest {
        private String categorySlug;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
        private Integer minDurationDays;
        private Integer maxDurationDays;
    }
}