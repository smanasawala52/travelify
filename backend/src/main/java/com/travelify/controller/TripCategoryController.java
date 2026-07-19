package com.travelify.controller;

import com.travelify.dto.TripCategoryDtos;
import com.travelify.model.User;
import com.travelify.service.TripCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Trip Categories", description = "Trip category management")
public class TripCategoryController {

    private final TripCategoryService tripCategoryService;

    public TripCategoryController(TripCategoryService tripCategoryService) {
        this.tripCategoryService = tripCategoryService;
    }

    @PostMapping
    @Operation(summary = "Create a new trip category")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public TripCategoryDtos.TripCategoryResponse create(
            @Valid @RequestBody TripCategoryDtos.TripCategoryRequest request,
            Authentication authentication) {
        User actor = (User) authentication.getPrincipal();
        return tripCategoryService.create(request, actor);
    }

    @PutMapping("/{categoryId}")
    @Operation(summary = "Update a trip category")
    @PreAuthorize("hasRole('ADMIN')")
    public TripCategoryDtos.TripCategoryResponse update(
            @PathVariable Long categoryId,
            @Valid @RequestBody TripCategoryDtos.TripCategoryRequest request,
            Authentication authentication) {
        User actor = (User) authentication.getPrincipal();
        return tripCategoryService.update(categoryId, request, actor);
    }

    @DeleteMapping("/{categoryId}")
    @Operation(summary = "Delete a trip category")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(
            @PathVariable Long categoryId,
            Authentication authentication) {
        User actor = (User) authentication.getPrincipal();
        tripCategoryService.delete(categoryId, actor);
    }

    @GetMapping("/{categoryId}")
    @Operation(summary = "Get a single category by ID")
    public TripCategoryDtos.TripCategoryResponse getById(
            @PathVariable Long categoryId) {
        return tripCategoryService.getById(categoryId);
    }

    @GetMapping
    @Operation(summary = "List all active trip categories")
    public List<TripCategoryDtos.TripCategoryResponse> listAll() {
        return tripCategoryService.listAll();
    }

    @GetMapping("/all")
    @Operation(summary = "List all trip categories (including inactive)")
    @PreAuthorize("hasRole('ADMIN')")
    public List<TripCategoryDtos.TripCategoryResponse> listAllIncludingInactive() {
        return tripCategoryService.listAllIncludingInactive();
    }
}
