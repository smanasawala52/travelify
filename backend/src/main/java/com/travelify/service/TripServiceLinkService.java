package com.travelify.service;

import com.travelify.dto.TripServiceLinkDtos;
import com.travelify.exception.ResourceConflictException;
import com.travelify.exception.ResourceNotFoundException;
import com.travelify.exception.ValidationException;
import com.travelify.model.AgentTrip;
import com.travelify.model.PublishStatus;
import com.travelify.model.TripService;
import com.travelify.model.User;
import com.travelify.repository.AgentTripRepository;
import com.travelify.repository.ServiceRepository;
import com.travelify.repository.TripServiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

/**
 * Manages optional service add-ons attached to agent trips.
 */
@Service
public class TripServiceLinkService {

    private final TripServiceRepository tripServiceRepository;
    private final AgentTripRepository agentTripRepository;
    private final ServiceRepository serviceRepository;
    private final TripAccessService tripAccessService;

    public TripServiceLinkService(TripServiceRepository tripServiceRepository,
                                  AgentTripRepository agentTripRepository,
                                  ServiceRepository serviceRepository,
                                  TripAccessService tripAccessService) {
        this.tripServiceRepository = tripServiceRepository;
        this.agentTripRepository = agentTripRepository;
        this.serviceRepository = serviceRepository;
        this.tripAccessService = tripAccessService;
    }

    @Transactional
    public TripServiceLinkDtos.TripServiceResponse addServiceToTrip(Long agentTripId,
                                                                    Long serviceId,
                                                                    BigDecimal overridePrice,
                                                                    Boolean isOptional,
                                                                    User actor) {
        AgentTrip trip = findTrip(agentTripId);
        tripAccessService.requireTripOwnerOrAdmin(actor, trip);

        com.travelify.model.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service", serviceId));

        if (service.getStatus() == PublishStatus.ARCHIVED) {
            throw new ValidationException("Cannot attach an archived service");
        }
        if (tripServiceRepository.existsByAgentTripIdAndServiceId(agentTripId, serviceId)) {
            throw new ResourceConflictException("Service is already attached to this trip");
        }
        if (overridePrice != null && overridePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("overridePrice must be >= 0");
        }

        TripService link = TripService.builder()
                .agentTrip(trip)
                .service(service)
                .isOptional(isOptional == null || isOptional)
                .overridePrice(overridePrice)
                .build();

        return toResponse(tripServiceRepository.save(link));
    }

    @Transactional
    public TripServiceLinkDtos.TripServiceResponse addServiceToTrip(Long agentTripId,
                                                                    TripServiceLinkDtos.AddServiceToTripRequest request,
                                                                    User actor) {
        if (request == null || request.getServiceId() == null) {
            throw new ValidationException("serviceId is required");
        }
        return addServiceToTrip(
                agentTripId,
                request.getServiceId(),
                request.getOverridePrice(),
                request.getIsOptional(),
                actor
        );
    }

    @Transactional
    public void removeServiceFromTrip(Long agentTripId, Long serviceId, User actor) {
        AgentTrip trip = findTrip(agentTripId);
        tripAccessService.requireTripOwnerOrAdmin(actor, trip);

        TripService link = tripServiceRepository.findByAgentTripIdAndServiceId(agentTripId, serviceId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Service link not found for trip " + agentTripId + " and service " + serviceId));
        tripServiceRepository.delete(link);
    }

    @Transactional
    public TripServiceLinkDtos.TripServiceResponse updateTripService(Long agentTripId,
                                                                     Long serviceId,
                                                                     TripServiceLinkDtos.UpdateTripServiceRequest request,
                                                                     User actor) {
        AgentTrip trip = findTrip(agentTripId);
        tripAccessService.requireTripOwnerOrAdmin(actor, trip);

        TripService link = tripServiceRepository.findByAgentTripIdAndServiceId(agentTripId, serviceId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Service link not found for trip " + agentTripId + " and service " + serviceId));

        if (request != null) {
            if (request.getIsOptional() != null) {
                link.setIsOptional(request.getIsOptional());
            }
            if (Boolean.TRUE.equals(request.getClearOverridePrice())) {
                link.setOverridePrice(null);
            } else if (request.getOverridePrice() != null) {
                if (request.getOverridePrice().compareTo(BigDecimal.ZERO) < 0) {
                    throw new ValidationException("overridePrice must be >= 0");
                }
                link.setOverridePrice(request.getOverridePrice());
            }
        }

        return toResponse(link);
    }

    @Transactional(readOnly = true)
    public List<TripServiceLinkDtos.TripServiceResponse> listTripServices(Long agentTripId, User actor) {
        AgentTrip trip = findTrip(agentTripId);
        tripAccessService.assertCanViewTrip(actor, trip);

        return tripServiceRepository.findByAgentTripId(agentTripId).stream()
                .map(this::toResponse)
                .toList();
    }

    private AgentTrip findTrip(Long agentTripId) {
        return agentTripRepository.findById(agentTripId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent trip", agentTripId));
    }

    private TripServiceLinkDtos.TripServiceResponse toResponse(TripService link) {
        com.travelify.model.Service service = link.getService();
        BigDecimal base = service != null ? service.getPrice() : null;
        BigDecimal effective = link.getOverridePrice() != null ? link.getOverridePrice() : base;

        return TripServiceLinkDtos.TripServiceResponse.builder()
                .id(link.getId())
                .agentTripId(link.getAgentTrip() != null ? link.getAgentTrip().getId() : null)
                .serviceId(service != null ? service.getId() : null)
                .serviceName(service != null ? service.getName() : null)
                .serviceType(service != null ? service.getServiceType() : null)
                .serviceDescription(service != null ? service.getDescription() : null)
                .servicePrice(base)
                .serviceCurrency(service != null ? service.getCurrency() : null)
                .serviceMeta(service != null && service.getMeta() != null
                        ? new HashMap<>(service.getMeta())
                        : new HashMap<>())
                .serviceStatus(service != null ? service.getStatus() : null)
                .providerId(service != null && service.getProvider() != null
                        ? service.getProvider().getId() : null)
                .providerEmail(service != null && service.getProvider() != null
                        ? service.getProvider().getEmail() : null)
                .isOptional(link.getIsOptional())
                .overridePrice(link.getOverridePrice())
                .effectivePrice(effective)
                .createdAt(link.getCreatedAt())
                .build();
    }
}
