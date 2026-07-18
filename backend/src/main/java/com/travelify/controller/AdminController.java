package com.travelify.controller;

import com.travelify.dto.BookingDtos;
import com.travelify.dto.PackageDtos;
import com.travelify.service.BookingService;
import com.travelify.service.PackageService;
import com.travelify.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin", description = "Admin overview and catalog/booking listings")
public class AdminController {
    private final UserService userService;
    private final PackageService packageService;
    private final BookingService bookingService;

    public AdminController(UserService userService,
                           PackageService packageService,
                           BookingService bookingService) {
        this.userService = userService;
        this.packageService = packageService;
        this.bookingService = bookingService;
    }

    @GetMapping("/packages")
    @Operation(summary = "List all packages")
    public List<PackageDtos.PackageResponse> packages() {
        return packageService.listAll();
    }

    @GetMapping("/bookings")
    @Operation(summary = "List all bookings")
    public List<BookingDtos.BookingResponse> bookings() {
        return bookingService.all();
    }

    @GetMapping("/overview")
    @Operation(summary = "Platform overview counts")
    public Map<String, Object> overview() {
        Map<String, Object> map = new HashMap<>();
        map.put("users", userService.listAll().size());
        map.put("packages", packageService.listAll().size());
        map.put("bookings", bookingService.all().size());
        return map;
    }
}
