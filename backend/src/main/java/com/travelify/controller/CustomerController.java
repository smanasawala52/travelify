package com.travelify.controller;

import com.travelify.dto.BookingDtos;
import com.travelify.service.BookingService;
import com.travelify.service.UserService; // Import UserService
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {
    private final BookingService bookingService;
    private final UserService userService; // Inject UserService

    public CustomerController(BookingService bookingService, UserService userService) {
        this.bookingService = bookingService;
        this.userService = userService;
    }

    @GetMapping("/bookings")
    public List<BookingDtos.BookingDetailResponse> myBookings(Authentication authentication) {
        Long userId = userService.findByEmail(authentication.getName()).getId(); // Get userId
        return bookingService.listBookings(userId, null, "customer");
    }

    @PostMapping("/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingDtos.BookingDetailResponse book(@Valid @RequestBody BookingDtos.CreateBookingRequest request, // Changed to CreateBookingRequest
                                            Authentication authentication) {
        Long userId = userService.findByEmail(authentication.getName()).getId(); // Get userId
        return bookingService.createBooking(request, userId); // Changed to createBooking
    }
}
