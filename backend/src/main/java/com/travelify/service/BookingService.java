package com.travelify.service;

import com.travelify.dto.BookingDtos;
import com.travelify.exception.*;
import com.travelify.model.*;
import com.travelify.model.Booking.BookingStatus;
import com.travelify.model.Booking.PaymentStatus;
import com.travelify.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingCustomerRepository bookingCustomerRepository;
    private final BookingAddonRepository bookingAddonRepository;
    private final BookingStatusHistoryRepository bookingStatusHistoryRepository;
    private final AgentTripDepartureRepository agentTripDepartureRepository;
    private final AgentTripRepository agentTripRepository;
    private final UserRepository userRepository;
    private final TripServiceRepository tripServiceRepository;

    private final InventoryService inventoryService;
    private final CouponService couponService;
    private final NotificationService notificationService;
    // private final PaymentService paymentService; // Uncomment when PaymentService is fully integrated

    @Transactional
    public BookingDtos.BookingDetailResponse createBooking(BookingDtos.CreateBookingRequest request, Long userId) {
        User customer = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found."));
        AgentTrip agentTrip = agentTripRepository.findById(request.getAgentTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Agent Trip not found."));
        AgentTripDeparture departure = agentTripDepartureRepository.findById(request.getDepartureId())
                .orElseThrow(() -> new ResourceNotFoundException("Departure not found."));

        // 1. Validate departure availability and reserve seats
        int totalTravelers = request.getAdults() + request.getChildren(); // Infants don't take seats
        inventoryService.reserveSeats(departure.getId(), totalTravelers);

        // 2. Calculate base price
        BigDecimal basePrice = departure.getPriceOverride() != null ?
                departure.getPriceOverride() : agentTrip.getPricing().stream()
                .filter(p -> p.getMinParticipants() <= totalTravelers && p.getMaxParticipants() >= totalTravelers)
                .map(AgentTripPricing::getPricePerPerson)
                .findFirst()
                .orElseThrow(() -> new ValidationException("No suitable pricing found for the number of travelers."))
                .multiply(BigDecimal.valueOf(totalTravelers));

        BigDecimal addonsPrice = BigDecimal.ZERO;
        if (request.getAddons() != null && !request.getAddons().isEmpty()) {
            for (BookingDtos.BookingAddonDto addonDto : request.getAddons()) {
                TripService tripService = tripServiceRepository.findById(addonDto.getTripServiceId())
                        .orElseThrow(() -> new ResourceNotFoundException("Trip service not found: " + addonDto.getTripServiceId()));
                addonsPrice = addonsPrice.add(tripService.getPrice().multiply(BigDecimal.valueOf(addonDto.getQuantity())));
            }
        }

        BigDecimal totalBeforeDiscount = basePrice.add(addonsPrice);
        BigDecimal discountAmount = BigDecimal.ZERO;

        // 3. Apply coupon if provided
        if (request.getCouponCode() != null && !request.getCouponCode().isEmpty()) {
            discountAmount = couponService.validateCoupon(request.getCouponCode(), totalBeforeDiscount);
            totalBeforeDiscount = totalBeforeDiscount.subtract(discountAmount);
        }

        // 4. Create Booking
        Booking booking = new Booking();
        booking.setBookingReference(generateBookingReference());
        booking.setUser(customer);
        booking.setAgentTrip(agentTrip);
        booking.setDeparture(departure);
        booking.setTotalPrice(totalBeforeDiscount);
        booking.setCurrency(agentTrip.getPricing().isEmpty() ? "USD" : agentTrip.getPricing().get(0).getCurrency()); // Assuming currency from pricing
        booking.setStatus(BookingStatus.PENDING);
        booking.setPaymentStatus(PaymentStatus.PENDING);
        booking.setAdults(request.getAdults());
        booking.setChildren(request.getChildren());
        booking.setInfants(request.getInfants());
        booking.setSpecialRequests(request.getSpecialRequests());
        booking.setCouponCode(request.getCouponCode());
        booking.setDiscountAmount(discountAmount);
        booking.setBookedAt(Instant.now());
        booking.setPaymentDueDate(Instant.now().plus(48, ChronoUnit.HOURS)); // Example: 48 hours to pay

        booking = bookingRepository.save(booking);

        // 5. Save traveler details
        for (BookingDtos.BookingCustomerDto travelerDto : request.getTravelers()) {
            BookingCustomer bookingCustomer = new BookingCustomer();
            bookingCustomer.setBooking(booking);
            bookingCustomer.setFirstName(travelerDto.getFirstName());
            bookingCustomer.setLastName(travelerDto.getLastName());
            bookingCustomer.setEmail(travelerDto.getEmail());
            bookingCustomer.setPhone(travelerDto.getPhone());
            bookingCustomer.setAge(travelerDto.getAge());
            bookingCustomer.setPassportNumber(travelerDto.getPassportNumber());
            bookingCustomerRepository.save(bookingCustomer);
        }

        // 6. Save add-ons
        if (request.getAddons() != null && !request.getAddons().isEmpty()) {
            for (BookingDtos.BookingAddonDto addonDto : request.getAddons()) {
                TripService tripService = tripServiceRepository.findById(addonDto.getTripServiceId()).get(); // Already checked existence
                BookingAddon bookingAddon = new BookingAddon();
                bookingAddon.setBooking(booking);
                bookingAddon.setTripService(tripService);
                bookingAddon.setQuantity(addonDto.getQuantity());
                bookingAddon.setPriceAtBooking(tripService.getPrice()); // Store price at booking time
                bookingAddonRepository.save(bookingAddon);
            }
        }

        // 7. Log initial status
        logBookingStatusChange(booking, null, BookingStatus.PENDING, customer, "Booking created.");

        // 8. Increment coupon usage if applied
        if (request.getCouponCode() != null && !request.getCouponCode().isEmpty()) {
            couponService.incrementUsage(request.getCouponCode());
        }

        // 9. Send confirmation email
        notificationService.sendBookingConfirmation(booking.getId());

        return convertToBookingDetailResponse(booking);
    }

    public BookingDtos.BookingDetailResponse getBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found."));
        return convertToBookingDetailResponse(booking);
    }

    public List<BookingDtos.BookingDetailResponse> listBookings(Long userId, BookingStatus status, String roleFilter) {
        // This method would need more complex logic based on the roleFilter (customer, agent, admin)
        // For simplicity, let's assume it's for a customer viewing their own bookings for now.
        // In a real app, you'd have different queries based on roles and permissions.
        List<Booking> bookings;
        if (userId != null) {
            if (status != null) {
                bookings = bookingRepository.findByUserIdAndStatus(userId, status);
            } else {
                bookings = bookingRepository.findByUserId(userId);
            }
        } else {
            // Admin view, or agent view (needs more filtering)
            if (status != null) {
                bookings = bookingRepository.findByStatus(status);
            } else {
                bookings = bookingRepository.findAll();
            }
        }
        return bookings.stream()
                .map(this::convertToBookingDetailResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public BookingDtos.BookingDetailResponse updateBookingStatus(Long bookingId, BookingDtos.UpdateBookingStatusRequest request, Long changedByUserId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found."));
        User changedBy = userRepository.findById(changedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        BookingStatus oldStatus = booking.getStatus();
        BookingStatus newStatus = request.getNewStatus();

        // Validate status transitions
        if (oldStatus == BookingStatus.CANCELLED || oldStatus == BookingStatus.COMPLETED) {
            throw new ForbiddenOperationException("Cannot change status of a cancelled or completed booking.");
        }
        if (newStatus == BookingStatus.CONFIRMED && booking.getPaymentStatus() != PaymentStatus.PAID) {
            throw new ForbiddenOperationException("Booking cannot be confirmed without payment.");
        }

        booking.setStatus(newStatus);
        if (newStatus == BookingStatus.CONFIRMED) {
            booking.setConfirmedAt(Instant.now());
        } else if (newStatus == BookingStatus.CANCELLED) {
            booking.setCancelledAt(Instant.now());
            // Release seats if cancelled
            inventoryService.releaseSeats(booking.getDeparture().getId(), booking.getAdults() + booking.getChildren());
        }
        booking = bookingRepository.save(booking);

        logBookingStatusChange(booking, oldStatus, newStatus, changedBy, request.getNote());
        notificationService.sendBookingStatusUpdate(booking.getId(), newStatus);

        return convertToBookingDetailResponse(booking);
    }

    @Transactional
    public void cancelBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ForbiddenOperationException("Booking is already cancelled.");
        }

        // If booking is paid, initiate refund process
        if (booking.getPaymentStatus() == PaymentStatus.PAID) {
            // This would typically involve calling PaymentService.refundPayment
            // For now, we'll just change the status.
            // paymentService.refundPayment(bookingId); // Uncomment when PaymentService is fully integrated
            booking.setPaymentStatus(PaymentStatus.REFUNDED);
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(Instant.now());
        bookingRepository.save(booking);

        // Restore seat availability
        inventoryService.releaseSeats(booking.getDeparture().getId(), booking.getAdults() + booking.getChildren());

        logBookingStatusChange(booking, booking.getStatus(), BookingStatus.CANCELLED, user, "Booking cancelled by user.");
        notificationService.sendBookingStatusUpdate(booking.getId(), BookingStatus.CANCELLED);
    }

    @Transactional
    public BookingDtos.BookingDetailResponse applyCoupon(Long bookingId, String couponCode, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        if (booking.getCouponCode() != null && !booking.getCouponCode().isEmpty()) {
            throw new ForbiddenOperationException("A coupon has already been applied to this booking.");
        }

        BigDecimal currentTotal = calculateTotal(booking);
        BigDecimal discountAmount = couponService.validateCoupon(couponCode, currentTotal);

        booking.setCouponCode(couponCode);
        booking.setDiscountAmount(discountAmount);
        booking.setTotalPrice(currentTotal.subtract(discountAmount));
        bookingRepository.save(booking);

        couponService.incrementUsage(couponCode);
        logBookingStatusChange(booking, booking.getStatus(), booking.getStatus(), user, "Coupon " + couponCode + " applied.");

        return convertToBookingDetailResponse(booking);
    }

    public BigDecimal calculateTotal(Booking booking) {
        BigDecimal basePrice = booking.getDeparture().getPriceOverride() != null ?
                booking.getDeparture().getPriceOverride() : booking.getAgentTrip().getPricing().stream()
                .filter(p -> p.getMinParticipants() <= (booking.getAdults() + booking.getChildren()) && p.getMaxParticipants() >= (booking.getAdults() + booking.getChildren()))
                .map(AgentTripPricing::getPricePerPerson)
                .findFirst()
                .orElseThrow(() -> new ValidationException("No suitable pricing found for the number of travelers."))
                .multiply(BigDecimal.valueOf(booking.getAdults() + booking.getChildren()));

        BigDecimal addonsPrice = booking.getBookingAddons().stream()
                .map(addon -> addon.getPriceAtBooking().multiply(BigDecimal.valueOf(addon.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return basePrice.add(addonsPrice);
    }

    private String generateBookingReference() {
        // Example: TRF-YYYY-XXXX (TRF-2026-0001)
        String year = String.valueOf(Instant.now().atZone(java.time.ZoneOffset.UTC).getYear());
        String uuid = UUID.randomUUID().toString().substring(0, 4).toUpperCase(); // Short unique part
        return "TRF-" + year + "-" + uuid;
    }

    private void logBookingStatusChange(Booking booking, BookingStatus fromStatus, BookingStatus toStatus, User changedBy, String note) {
        BookingStatusHistory history = new BookingStatusHistory();
        history.setBooking(booking);
        history.setStatusFrom(fromStatus != null ? fromStatus.name() : null);
        history.setStatusTo(toStatus.name());
        history.setChangedBy(changedBy);
        history.setNote(note);
        bookingStatusHistoryRepository.save(history);
    }

    private BookingDtos.BookingDetailResponse convertToBookingDetailResponse(Booking booking) {
        BookingDtos.BookingDetailResponse response = new BookingDtos.BookingDetailResponse();
        response.setId(booking.getId());
        response.setBookingReference(booking.getBookingReference());
        response.setUserId(booking.getUser().getId());
        response.setUserEmail(booking.getUser().getEmail());
        response.setAgentTripId(booking.getAgentTrip().getId());
        response.setAgentTripTitle(booking.getAgentTrip().getTitle());
        response.setDepartureId(booking.getDeparture().getId());
        response.setDepartureDate(booking.getDeparture().getDepartureDate());
        response.setTotalPrice(booking.getTotalPrice());
        response.setCurrency(booking.getCurrency());
        response.setStatus(booking.getStatus());
        response.setPaymentStatus(booking.getPaymentStatus());
        response.setAdults(booking.getAdults());
        response.setChildren(booking.getChildren());
        response.setInfants(booking.getInfants());
        response.setSpecialRequests(booking.getSpecialRequests());
        response.setCouponCode(booking.getCouponCode());
        response.setDiscountAmount(booking.getDiscountAmount());
        response.setBookedAt(booking.getBookedAt());
        response.setConfirmedAt(booking.getConfirmedAt());
        response.setCancelledAt(booking.getCancelledAt());
        response.setPaymentDueDate(booking.getPaymentDueDate());

        response.setTravelers(booking.getBookingCustomers().stream()
                .map(customer -> {
                    BookingDtos.BookingCustomerDto dto = new BookingDtos.BookingCustomerDto();
                    dto.setFirstName(customer.getFirstName());
                    dto.setLastName(customer.getLastName());
                    dto.setEmail(customer.getEmail());
                    dto.setPhone(customer.getPhone());
                    dto.setAge(customer.getAge());
                    dto.setPassportNumber(customer.getPassportNumber());
                    return dto;
                })
                .collect(Collectors.toList()));

        response.setAddons(booking.getBookingAddons().stream()
                .map(addon -> {
                    BookingDtos.BookingAddonDetailDto dto = new BookingDtos.BookingAddonDetailDto();
                    dto.setId(addon.getId());
                    dto.setTripServiceId(addon.getTripService().getId());
                    dto.setServiceName(addon.getTripService().getName());
                    dto.setServiceDescription(addon.getTripService().getDescription());
                    dto.setServicePrice(addon.getTripService().getPrice());
                    dto.setQuantity(addon.getQuantity());
                    dto.setPriceAtBooking(addon.getPriceAtBooking());
                    return dto;
                })
                .collect(Collectors.toList()));

        response.setPayments(booking.getPayments().stream()
                .map(payment -> {
                    BookingDtos.PaymentDto dto = new BookingDtos.PaymentDto();
                    dto.setId(payment.getId());
                    dto.setBookingId(payment.getBooking().getId());
                    dto.setAmount(payment.getAmount());
                    dto.setCurrency(payment.getCurrency());
                    dto.setPaymentMethod(payment.getPaymentMethod());
                    dto.setTransactionId(payment.getTransactionId());
                    dto.setStatus(payment.getStatus());
                    dto.setPaidAt(payment.getPaidAt());
                    return dto;
                })
                .collect(Collectors.toList()));

        response.setStatusHistory(booking.getStatusHistory().stream()
                .map(history -> {
                    BookingDtos.BookingStatusHistoryDto dto = new BookingDtos.BookingStatusHistoryDto();
                    dto.setId(history.getId());
                    dto.setBookingId(history.getBooking().getId());
                    dto.setStatusFrom(history.getStatusFrom());
                    dto.setStatusTo(history.getStatusTo());
                    dto.setChangedByUserId(history.getChangedBy().getId());
                    dto.setChangedByUserEmail(history.getChangedBy().getEmail());
                    dto.setChangedAt(history.getChangedAt());
                    dto.setNote(history.getNote());
                    return dto;
                })
                .collect(Collectors.toList()));

        return response;
    }
}
