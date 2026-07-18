package com.travelify.service;

import com.travelify.dto.ServiceDtos;
import com.travelify.exception.ResourceNotFoundException;
import com.travelify.exception.ValidationException;
import com.travelify.model.PublishStatus;
import com.travelify.model.Role;
import com.travelify.model.ServiceType;
import com.travelify.model.User;
import com.travelify.repository.ServiceRepository;
import com.travelify.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Provider-facing service catalog (hotels, insurance, visa, custom).
 */
@org.springframework.stereotype.Service
public class ServiceService {

    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final TripAccessService tripAccessService;

    public ServiceService(ServiceRepository serviceRepository,
                          UserRepository userRepository,
                          TripAccessService tripAccessService) {
        this.serviceRepository = serviceRepository;
        this.userRepository = userRepository;
        this.tripAccessService = tripAccessService;
    }

    @Transactional
    public ServiceDtos.ServiceResponse createService(Long providerId,
                                                     ServiceDtos.ServiceRequest request,
                                                     User actor) {
        tripAccessService.requireAgentOrAdmin(actor);
        validateRequest(request);
        User provider = resolveProvider(actor, providerId);

        com.travelify.model.Service entity = com.travelify.model.Service.builder()
                .provider(provider)
                .serviceType(request.getServiceType())
                .name(request.getName().trim())
                .description(request.getDescription())
                .price(request.getPrice())
                .currency(normalizeCurrency(request.getCurrency()))
                .meta(copyMeta(request.getMeta()))
                .status(request.getStatus() == null ? PublishStatus.DRAFT : request.getStatus())
                .build();

        return toResponse(serviceRepository.save(entity));
    }

    @Transactional
    public ServiceDtos.ServiceResponse updateService(Long serviceId,
                                                     ServiceDtos.ServiceRequest request,
                                                     User actor) {
        validateRequest(request);
        com.travelify.model.Service entity = findEntity(serviceId);
        tripAccessService.requireServiceOwnerOrAdmin(actor, entity);

        entity.setServiceType(request.getServiceType());
        entity.setName(request.getName().trim());
        entity.setDescription(request.getDescription());
        entity.setPrice(request.getPrice());
        entity.setCurrency(normalizeCurrency(request.getCurrency()));
        if (request.getMeta() != null) {
            entity.setMeta(copyMeta(request.getMeta()));
        }
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
        return toResponse(entity);
    }

    @Transactional
    public ServiceDtos.ServiceResponse deleteService(Long serviceId, User actor) {
        com.travelify.model.Service entity = findEntity(serviceId);
        tripAccessService.requireServiceOwnerOrAdmin(actor, entity);
        entity.setStatus(PublishStatus.ARCHIVED);
        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public ServiceDtos.ServiceResponse getService(Long serviceId, User actor) {
        com.travelify.model.Service entity = findEntity(serviceId);
        if (entity.getStatus() != PublishStatus.PUBLISHED) {
            // draft/archived visible only to owner or admin
            if (actor == null) {
                throw new ResourceNotFoundException("Service", serviceId);
            }
            tripAccessService.requireServiceOwnerOrAdmin(actor, entity);
        }
        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public Page<ServiceDtos.ServiceResponse> listServices(Long providerId,
                                                          ServiceType serviceType,
                                                          PublishStatus status,
                                                          String search,
                                                          User actor,
                                                          Pageable pageable) {
        Long effectiveProviderId = providerId;
        PublishStatus effectiveStatus = status;

        if (actor == null || actor.getRole() == Role.CUSTOMER) {
            effectiveStatus = PublishStatus.PUBLISHED;
        } else if (actor.getRole() == Role.AGENT) {
            // providers list their own catalog by default
            if (effectiveProviderId == null) {
                effectiveProviderId = actor.getId();
            } else if (!effectiveProviderId.equals(actor.getId()) && effectiveStatus != PublishStatus.PUBLISHED) {
                // can browse others' published services only
                effectiveStatus = PublishStatus.PUBLISHED;
            }
        }
        // admin: unrestricted filters

        return serviceRepository
                .search(effectiveProviderId, serviceType, effectiveStatus, blankToNull(search), pageable)
                .map(this::toResponse);
    }

    private User resolveProvider(User actor, Long providerId) {
        if (actor.getRole() == Role.AGENT) {
            if (providerId != null && !providerId.equals(actor.getId())) {
                throw new ValidationException("Providers can only create services for themselves");
            }
            return actor;
        }
        if (providerId == null) {
            throw new ValidationException("providerId is required when admin creates a service");
        }
        User provider = userRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", providerId));
        if (provider.getRole() != Role.AGENT && provider.getRole() != Role.ADMIN) {
            throw new ValidationException("Service provider must be an AGENT user");
        }
        return provider;
    }

    private com.travelify.model.Service findEntity(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service", id));
    }

    private void validateRequest(ServiceDtos.ServiceRequest request) {
        if (request == null) {
            throw new ValidationException("Service request is required");
        }
        if (request.getServiceType() == null) {
            throw new ValidationException("serviceType is required");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new ValidationException("Service name is required");
        }
        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("price must be >= 0");
        }
    }

    private String normalizeCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            return "USD";
        }
        return currency.trim().toUpperCase();
    }

    private Map<String, Object> copyMeta(Map<String, Object> meta) {
        return meta == null ? new HashMap<>() : new HashMap<>(meta);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private ServiceDtos.ServiceResponse toResponse(com.travelify.model.Service entity) {
        User provider = entity.getProvider();
        return ServiceDtos.ServiceResponse.builder()
                .id(entity.getId())
                .providerId(provider != null ? provider.getId() : null)
                .providerEmail(provider != null ? provider.getEmail() : null)
                .providerBusinessName(provider != null ? provider.getBusinessName() : null)
                .serviceType(entity.getServiceType())
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .currency(entity.getCurrency())
                .meta(entity.getMeta() == null ? new HashMap<>() : new HashMap<>(entity.getMeta()))
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
