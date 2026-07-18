package com.travelify.controller;

import com.travelify.dto.BookingDtos;
import com.travelify.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {
    private final BookingService bookingService;

    public CustomerController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/bookings")
    public List<BookingDtos.BookingResponse> myBookings(Authentication authentication) {
        return bookingService.forCustomer(authentication.getName());
    }

    @PostMapping("/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingDtos.BookingResponse book(@Valid @RequestBody BookingDtos.BookingRequest request,
                                            Authentication authentication) {
        return bookingService.create(request, authentication.getName());
    }
}