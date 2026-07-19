package com.travelify.controller;

import com.travelify.dto.BookingDtos;
import com.travelify.dto.PackageDtos;
import com.travelify.model.Booking.BookingStatus;
import com.travelify.service.BookingService;
import com.travelify.service.PackageService;
import com.travelify.service.UserService; // Import UserService
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agent")
public class AgentController {
    private final PackageService packageService;
    private final BookingService bookingService;
    private final UserService userService; // Inject UserService

    public AgentController(PackageService packageService, BookingService bookingService, UserService userService) {
        this.packageService = packageService;
        this.bookingService = bookingService;
        this.userService = userService;
    }

    @GetMapping("/packages")
    public List<PackageDtos.PackageResponse> packages() {
        return packageService.listAll();
    }

    @PostMapping("/packages")
    @ResponseStatus(HttpStatus.CREATED)
    public PackageDtos.PackageResponse create(@Valid @RequestBody PackageDtos.PackageRequest request,
                                              Authentication authentication) {
        return packageService.create(request, authentication.getName());
    }

    @PutMapping("/packages/{id}")
    public PackageDtos.PackageResponse update(@PathVariable Long id,
                                              @Valid @RequestBody PackageDtos.PackageRequest request) {
        return packageService.update(id, request);
    }

    @GetMapping("/bookings")
    public List<BookingDtos.BookingDetailResponse> bookings(Authentication authentication) {
        Long userId = userService.findByEmail(authentication.getName()).getId();
        return bookingService.listBookings(userId, null, "agent"); // Assuming agent can only see their own trip bookings
    }

    @PatchMapping("/bookings/{id}/status")
    public BookingDtos.BookingDetailResponse updateStatus(@PathVariable Long id,
                                                    @RequestBody Map<String, String> body,
                                                    Authentication authentication) {
        BookingStatus status = BookingStatus.valueOf(body.get("status"));
        Long changedByUserId = userService.findByEmail(authentication.getName()).getId();
        BookingDtos.UpdateBookingStatusRequest updateRequest = new BookingDtos.UpdateBookingStatusRequest();
        updateRequest.setNewStatus(status);
        updateRequest.setNote(body.get("note")); // Assuming 'note' can be passed in the request body
        return bookingService.updateBookingStatus(id, updateRequest, changedByUserId);
    }
}
