package com.travelify.controller;

import com.travelify.dto.BookingDtos;
import com.travelify.dto.PackageDtos;
import com.travelify.model.BookingStatus;
import com.travelify.service.BookingService;
import com.travelify.service.PackageService;
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

    public AgentController(PackageService packageService, BookingService bookingService) {
        this.packageService = packageService;
        this.bookingService = bookingService;
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
    public List<BookingDtos.BookingResponse> bookings() {
        return bookingService.all();
    }

    @PatchMapping("/bookings/{id}/status")
    public BookingDtos.BookingResponse updateStatus(@PathVariable Long id,
                                                    @RequestBody Map<String, String> body) {
        BookingStatus status = BookingStatus.valueOf(body.get("status"));
        return bookingService.updateStatus(id, status);
    }
}