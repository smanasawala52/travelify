package com.travelify.controller;

import com.travelify.dto.ServiceDtos;
import com.travelify.model.PublishStatus;
import com.travelify.model.User;
import com.travelify.service.ServiceService;
import io.swagger.v3.oas.annotations.Operation;
import com.travelify.model.ServiceType;
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
@RequestMapping("/api/provider/services")
@Tag(name = "Provider Services", description = "Service provider service management")
public class ServiceController {

    private final ServiceService serviceService;

    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @PostMapping
    @Operation(summary = "Create a new service")
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceDtos.ServiceResponse create(
            @Valid @RequestBody ServiceDtos.ServiceRequest request,
            Authentication authentication) {
        User actor = (User) authentication.getPrincipal();
        return serviceService.createService(null, request, actor);
    }

    @PutMapping("/{serviceId}")
    @Operation(summary = "Update a service")
    public ServiceDtos.ServiceResponse update(
            @PathVariable Long serviceId,
            @Valid @RequestBody ServiceDtos.ServiceRequest request,
            Authentication authentication) {
        User actor = (User) authentication.getPrincipal();
        return serviceService.updateService(serviceId, request, actor);
    }

    @DeleteMapping("/{serviceId}")
    @Operation(summary = "Delete (archive) a service")
    public ServiceDtos.ServiceResponse delete(
            @PathVariable Long serviceId,
            Authentication authentication) {
        User actor = (User) authentication.getPrincipal();
        return serviceService.deleteService(serviceId, actor);
    }

    @GetMapping("/{serviceId}")
    @Operation(summary = "Get a single service by ID")
    public ServiceDtos.ServiceResponse getById(
            @PathVariable Long serviceId,
            Authentication authentication) {
        User actor = (User) authentication.getPrincipal();
        return serviceService.getService(serviceId, actor);
    }

    @GetMapping
    @Operation(summary = "List all services with filtering")
    public Page<ServiceDtos.ServiceResponse> list(
            @RequestParam(required = false) String serviceType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long providerId,
            @RequestParam(required = false) String search,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        User actor = (User) authentication.getPrincipal();
        ServiceType typeEnum = serviceType != null ? ServiceType.valueOf(serviceType) : null;
        PublishStatus statusEnum = status != null ? PublishStatus.valueOf(status) : null;
        return serviceService.listServices(providerId, typeEnum, statusEnum, search, actor, pageable);
    }

    @GetMapping("/my")
    @Operation(summary = "List services for the current provider")
    public Page<ServiceDtos.ServiceResponse> listMyServices(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        User actor = (User) authentication.getPrincipal();
        PublishStatus statusEnum = status != null ? PublishStatus.valueOf(status) : null;
        return serviceService.listServices(null, null, statusEnum, search, actor, pageable);
    }
}
