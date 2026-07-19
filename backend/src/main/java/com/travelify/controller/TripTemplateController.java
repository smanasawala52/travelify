package com.travelify.controller;

import com.travelify.dto.TripTemplateDtos;
import com.travelify.model.PublishStatus;
import com.travelify.model.User;
import com.travelify.service.TripTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/templates")
@Tag(name = "Trip Templates", description = "Admin trip template management")
public class TripTemplateController {

    private final TripTemplateService tripTemplateService;

    public TripTemplateController(TripTemplateService tripTemplateService) {
        this.tripTemplateService = tripTemplateService;
    }

    @PostMapping
    @Operation(summary = "Create a new trip template")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public TripTemplateDtos.TripTemplateResponse create(
            @Valid @RequestBody TripTemplateDtos.TripTemplateRequest request,
            Authentication authentication) {
        User actor = (User) authentication.getPrincipal();
        return tripTemplateService.createTemplate(actor, request);
    }

    @PutMapping("/{templateId}")
    @Operation(summary = "Update a trip template")
    @PreAuthorize("hasRole('ADMIN')")
    public TripTemplateDtos.TripTemplateResponse update(
            @PathVariable Long templateId,
            @Valid @RequestBody TripTemplateDtos.TripTemplateRequest request,
            Authentication authentication) {
        User actor = (User) authentication.getPrincipal();
        return tripTemplateService.updateTemplate(actor, templateId, request);
    }

    @DeleteMapping("/{templateId}")
    @Operation(summary = "Delete (archive) a trip template")
    @PreAuthorize("hasRole('ADMIN')")
    public TripTemplateDtos.TripTemplateResponse delete(
            @PathVariable Long templateId,
            Authentication authentication) {
        User actor = (User) authentication.getPrincipal();
        return tripTemplateService.deleteTemplate(actor, templateId);
    }

    @GetMapping("/{templateId}")
    @Operation(summary = "Get a single template by ID")
    public TripTemplateDtos.TripTemplateResponse getById(
            @PathVariable Long templateId) {
        return tripTemplateService.getTemplate(templateId);
    }

    @GetMapping
    @Operation(summary = "List all trip templates with filtering")
    public Page<TripTemplateDtos.TripTemplateResponse> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false) String search,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        PublishStatus statusEnum = status != null ? PublishStatus.valueOf(status) : null;
        return tripTemplateService.listTemplates(pageable, statusEnum, categoryId, featured, search);
    }

    @PostMapping("/{templateId}/duplicate")
    @Operation(summary = "Duplicate a trip template")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public TripTemplateDtos.TripTemplateResponse duplicate(
            @PathVariable Long templateId,
            Authentication authentication) {
        User actor = (User) authentication.getPrincipal();
        return tripTemplateService.duplicateTemplate(actor, templateId);
    }
}
