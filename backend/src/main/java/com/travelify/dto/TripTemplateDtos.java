package com.travelify.dto;

import com.travelify.model.PublishStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

public final class TripTemplateDtos {
    private TripTemplateDtos() {}

    @Data
    public static class TripTemplateRequest {
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
    }

    @Data
    @Builder
    public static class TripTemplateResponse {
        private Long id;
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
        private Long createdById;
        private String createdByEmail;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
