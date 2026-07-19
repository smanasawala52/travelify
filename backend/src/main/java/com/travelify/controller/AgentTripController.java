package com.travelify.controller;

import com.travelify.dto.AgentTripDtos;
import com.travelify.model.User;
import com.travelify.service.AgentTripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agent/trips")
@Tag(name = "Agent Trips", description = "Travel agent trip management")
public class AgentTripController {

    private final AgentTripService agentTripService;

    public AgentTripController(AgentTripService agentTripService) {
        this.agentTripService = agentTripService;
    }

    @PostMapping
    @Operation(summary = "Create a new trip from scratch")
    @ResponseStatus(HttpStatus.CREATED)
    public AgentTripDtos.AgentTripResponse createCustom(
            @Valid @RequestBody AgentTripDtos.AgentTripRequest request,
            Authentication authentication) {
        User actor = (User) authentication.getPrincipal();
        return agentTripService.createCustom(actor.getId(), request, actor);
    }

    @PostMapping("/from-template/{templateId}")
    @Operation(summary = "Create a new trip from a template")
    @ResponseStatus(HttpStatus.CREATED)
    public AgentTripDtos.AgentTripResponse createFromTemplate(
            @PathVariable Long templateId,
            @Valid @RequestBody AgentTripDtos.OverrideRequest overrides,
            Authentication authentication) {
        User actor = (User) authentication.getPrincipal();
        return agentTripService.createFromTemplate(templateId, null, overrides, actor);
    }

    @PutMapping("/{tripId}")
    @Operation(summary = "Update an existing trip")
    public AgentTripDtos.AgentTripResponse update(
            @PathVariable Long tripId,
            @Valid @RequestBody AgentTripDtos.AgentTripRequest request,
            Authentication authentication) {
        User actor = (User) authentication.getPrincipal();
        return agentTripService.updateTrip(tripId, request, actor);
    }

    @DeleteMapping("/{tripId}")
    @Operation(summary = "Delete (archive) a trip")
    public AgentTripDtos.AgentTripResponse delete(
            @PathVariable Long tripId,
            Authentication authentication) {
        User actor = (User) authentication.getPrincipal();
        return agentTripService.deleteTrip(tripId, actor);
    }

    @GetMapping("/{tripId}")
    @Operation(summary = "Get a single trip by ID")
    public AgentTripDtos.AgentTripResponse getById(
            @PathVariable Long tripId,
            Authentication authentication) {
        User actor = (User) authentication.getPrincipal();
        return agentTripService.getTrip(tripId, actor);
    }

    @GetMapping
    @Operation(summary = "List all agent trips with filtering")
    public Page<AgentTripDtos.AgentTripResponse> list(
            @RequestParam(required = false) Long agentId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false) String search,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        User actor = (User) authentication.getPrincipal();
        com.travelify.model.PublishStatus statusEnum = status != null 
                ? com.travelify.model.PublishStatus.valueOf(status) 
                : null;
        return agentTripService.listTrips(actor, agentId, statusEnum, categoryId, featured, search, pageable);
    }

    @PostMapping("/{tripId}/copy")
    @Operation(summary = "Copy an existing trip")
    @ResponseStatus(HttpStatus.CREATED)
    public AgentTripDtos.AgentTripResponse copy(
            @PathVariable Long tripId,
            Authentication authentication) {
        User actor = (User) authentication.getPrincipal();
        return agentTripService.copyTrip(tripId, actor);
    }
}
