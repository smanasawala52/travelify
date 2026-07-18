package com.travelify.dto;

import com.travelify.model.BookingStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class BookingDtos {
    private BookingDtos() {}

    @Data
    public static class BookingRequest {
        @NotNull
        private Long packageId;
        @NotNull
        private LocalDate travelDate;
        @NotNull @Min(1)
        private Integer travelers;
    }

    @Data
    @Builder
    public static class BookingResponse {
        private Long id;
        private Long packageId;
        private String packageTitle;
        private String destination;
        private LocalDate travelDate;
        private Integer travelers;
        private BigDecimal totalPrice;
        private BookingStatus status;
        private String customerEmail;
    }
}