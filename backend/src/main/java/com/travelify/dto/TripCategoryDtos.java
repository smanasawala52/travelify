package com.travelify.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

public final class TripCategoryDtos {
    private TripCategoryDtos() {}

    @Data
    public static class TripCategoryRequest {
        @NotBlank
        @Size(max = 100)
        private String name;

        @Size(max = 100)
        private String slug;

        @Size(max = 500)
        private String description;

        @Size(max = 100)
        private String icon;

        @Min(0)
        private Integer sortOrder;

        private Boolean isActive;
    }

    @Data
    @Builder
    public static class TripCategoryResponse {
        private Long id;
        private String name;
        private String slug;
        private String description;
        private String icon;
        private Integer sortOrder;
        private Boolean isActive;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
