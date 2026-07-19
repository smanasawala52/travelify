package com.travelify.dto;

import com.travelify.model.Booking;
import com.travelify.model.Booking.BookingStatus;
import com.travelify.model.Booking.PaymentStatus;
import com.travelify.model.Coupon.DiscountType;
import com.travelify.model.Payment;
import com.travelify.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor; // Added for PaymentInitiationResponse
import lombok.NoArgsConstructor; // Added for PaymentInitiationResponse

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class BookingDtos {
    private BookingDtos() {}

    // Existing DTOs (if any, keeping them for context, but new ones will be added below)
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

    // New DTOs for Module 3

    @Data
    public static class BookingCustomerDto {
        @NotBlank
        private String firstName;
        @NotBlank
        private String lastName;
        @Email
        private String email;
        private String phone;
        private Integer age;
        private String passportNumber;
    }

    @Data
    public static class BookingAddonDto {
        @NotNull
        private Long tripServiceId;
        @Min(1)
        private Integer quantity = 1;
    }

    @Data
    public static class CreateBookingRequest {
        @NotNull
        private Long agentTripId;
        @NotNull
        private Long departureId;
        @Min(1)
        private Integer adults = 1;
        private Integer children = 0;
        private Integer infants = 0;
        private String specialRequests;
        private String couponCode;
        @NotNull
        private List<BookingCustomerDto> travelers;
        private List<BookingAddonDto> addons; // Added this field
        // Optional: payment method for immediate payment, or just create PENDING booking
        private String preferredPaymentMethod;
    }

    @Data
    public static class BookingDetailResponse {
        private Long id;
        private String bookingReference;
        private Long userId;
        private String userEmail;
        private Long agentTripId;
        private String agentTripTitle;
        private Long departureId;
        private LocalDate departureDate;
        private BigDecimal totalPrice;
        private String currency;
        private BookingStatus status;
        private PaymentStatus paymentStatus;
        private Integer adults;
        private Integer children;
        private Integer infants;
        private String specialRequests;
        private String couponCode;
        private BigDecimal discountAmount;
        private Instant bookedAt;
        private Instant confirmedAt;
        private Instant cancelledAt;
        private Instant paymentDueDate;
        private List<BookingCustomerDto> travelers;
        private List<BookingAddonDetailDto> addons;
        private List<PaymentDto> payments;
        private List<BookingStatusHistoryDto> statusHistory;
    }

    @Data
    public static class BookingAddonDetailDto {
        private Long id;
        private Long tripServiceId;
        private String serviceName;
        private String serviceDescription;
        private BigDecimal servicePrice;
        private Integer quantity;
        private BigDecimal priceAtBooking;
    }

    @Data
    public static class PaymentDto {
        private Long id;
        private Long bookingId;
        private BigDecimal amount;
        private String currency;
        private String paymentMethod;
        private String transactionId;
        private Payment.PaymentStatus status;
        private Instant paidAt;
    }

    @Data
    public static class BookingStatusHistoryDto {
        private Long id;
        private Long bookingId;
        private String statusFrom;
        private String statusTo;
        private Long changedByUserId;
        private String changedByUserEmail;
        private Instant changedAt;
        private String note;
    }

    @Data
    public static class UpdateBookingStatusRequest {
        @NotNull
        private BookingStatus newStatus;
        private String note;
    }

    @Data
    public static class ApplyCouponRequest {
        @NotBlank
        private String couponCode;
    }

    @Data
    public static class CouponDto {
        private Long id;
        private String code;
        private DiscountType discountType;
        private BigDecimal discountValue;
        private BigDecimal minSpend;
        private BigDecimal maxDiscount;
        private LocalDate validFrom;
        private LocalDate validTo;
        private Integer usageLimit;
        private Integer usedCount;
        private Boolean isActive;
    }

    @Data
    @NoArgsConstructor // Ensure no-arg constructor is available
    @AllArgsConstructor // Add all-arg constructor for direct initialization
    public static class PaymentInitiationResponse {
        private String paymentIntentId; // For Stripe
        private String redirectUrl; // For PayPal or other redirects
        private String status;
        private String message;
    }

    @Data
    public static class PaymentWebhookRequest {
        @NotBlank
        private String payload;
        private String signature; // For verifying webhook
    }
}
