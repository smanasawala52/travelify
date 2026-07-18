package com.travelify.service;

import com.travelify.dto.AgentTripDtos;
import com.travelify.exception.ResourceNotFoundException;
import com.travelify.exception.ValidationException;
import com.travelify.model.AgentTrip;
import com.travelify.model.PublishStatus;
import com.travelify.model.Role;
import com.travelify.model.TripTemplate;
import com.travelify.model.User;
import com.travelify.repository.AgentTripRepository;
import com.travelify.repository.TripTemplateRepository;
import com.travelify.repository.UserRepository;
import com.travelify.util.SlugUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgentTripService {

    private final AgentTripRepository agentTripRepository;
    private final TripTemplateRepository tripTemplateRepository;
    private final UserRepository userRepository;
    private final TripCopyService tripCopyService;
    private final TripMergeService tripMergeService;
    private final TripAccessService tripAccessService;

    public AgentTripService(AgentTripRepository agentTripRepository,
                            TripTemplateRepository tripTemplateRepository,
                            UserRepository userRepository,
                            TripCopyService tripCopyService,
                            TripMergeService tripMergeService,
                            TripAccessService tripAccessService) {
        this.agentTripRepository = agentTripRepository;
        this.tripTemplateRepository = tripTemplateRepository;
        this.userRepository = userRepository;
        this.tripCopyService = tripCopyService;
        this.tripMergeService = tripMergeService;
        this.tripAccessService = tripAccessService;
    }

    @Transactional
    public AgentTripDtos.AgentTripResponse createFromTemplate(Long templateId,
                                                              Long agentId,
                                                              AgentTripDtos.OverrideRequest overrides,
                                                              User actor) {
        tripAccessService.requireAgentOrAdmin(actor);
        User agent = resolveAgentTarget(actor, agentId);
        TripTemplate template = tripTemplateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip template", templateId));

        if (template.getStatus() == PublishStatus.ARCHIVED) {
            throw new ValidationException("Cannot create a trip from an archived template");
        }

        AgentTrip trip = tripCopyService.copyFromTemplate(template, agent, overrides);
        return tripMergeService.toMergedResponse(agentTripRepository.save(trip));
    }

    @Transactional
    public AgentTripDtos.AgentTripResponse createCustom(Long agentId,
                                                        AgentTripDtos.AgentTripRequest request,
                                                        User actor) {
        tripAccessService.requireAgentOrAdmin(actor);
        validateCustomRequest(request);
        User agent = resolveAgentTarget(actor, agentId);

        String slugBase = request.getSlug() != null && !request.getSlug().isBlank()
                ? request.getSlug()
                : request.getTitle();
        String slug = tripCopyService.uniqueAgentSlug(agent.getId(), SlugUtils.slugify(slugBase), null);

        AgentTrip trip = AgentTrip.builder()
                .template(null)
                .agent(agent)
                .title(request.getTitle().trim())
                .slug(slug)
                .shortDescription(request.getShortDescription())
                .fullDescription(request.getFullDescription())
                .featuredImage(request.getFeaturedImage())
                .difficulty(request.getDifficulty())
                .durationDays(request.getDurationDays())
                .minAge(request.getMinAge())
                .maxGroupSize(request.getMaxGroupSize())
                .isFeatured(Boolean.TRUE.equals(request.getIsFeatured()))
                .status(request.getStatus() == null ? PublishStatus.DRAFT : request.getStatus())
                .build();

        tripCopyService.applyCustomRequest(trip, request, false);
        // re-apply title/slug from builder if applyCustom overwrote inconsistently
        if (trip.getTitle() == null || trip.getTitle().isBlank()) {
            trip.setTitle(request.getTitle().trim());
        }
        if (trip.getSlug() == null || trip.getSlug().isBlank()) {
            trip.setSlug(slug);
        }

        return tripMergeService.toMergedResponse(agentTripRepository.save(trip));
    }

    @Transactional
    public AgentTripDtos.AgentTripResponse updateTrip(Long tripId,
                                                      AgentTripDtos.AgentTripRequest request,
                                                      User actor) {
        AgentTrip trip = findEntity(tripId);
        tripAccessService.requireTripOwnerOrAdmin(actor, trip);

        boolean trackOverrides = trip.getTemplate() != null;
        tripCopyService.applyCustomRequest(trip, request, trackOverrides);

        if (request.getTitle() != null && (request.getTitle().isBlank())) {
            throw new ValidationException("title cannot be blank");
        }

        return tripMergeService.toMergedResponse(trip);
    }

    @Transactional
    public AgentTripDtos.AgentTripResponse deleteTrip(Long tripId, User actor) {
        AgentTrip trip = findEntity(tripId);
        tripAccessService.requireTripOwnerOrAdmin(actor, trip);
        trip.setStatus(PublishStatus.ARCHIVED);
        return tripMergeService.toMergedResponse(trip);
    }

    @Transactional(readOnly = true)
    public AgentTripDtos.AgentTripResponse getTrip(Long tripId, User actor) {
        AgentTrip trip = findEntity(tripId);
        tripAccessService.assertCanViewTrip(actor, trip);
        return tripMergeService.toMergedResponse(trip);
    }

    /**
     * List trips with role-aware visibility:
     * <ul>
     *   <li>public / customer — published only</li>
     *   <li>agent — own trips (optional status/category filters)</li>
     *   <li>admin — all trips (optional agentId filter)</li>
     * </ul>
     */
    @Transactional(readOnly = true)
    public Page<AgentTripDtos.AgentTripResponse> listTrips(User actor,
                                                           Long agentId,
                                                           PublishStatus status,
                                                           Long categoryId,
                                                           Boolean featured,
                                                           String search,
                                                           Pageable pageable) {
        Long effectiveAgentId = agentId;
        PublishStatus effectiveStatus = status;

        if (actor == null || actor.getRole() == Role.CUSTOMER) {
            effectiveStatus = PublishStatus.PUBLISHED;
            // public catalog: ignore agent ownership filter unless explicitly requested
        } else if (actor.getRole() == Role.AGENT) {
            effectiveAgentId = actor.getId();
        } else if (actor.getRole() == Role.ADMIN) {
            // admin keeps provided agentId / status filters
        }

        return agentTripRepository
                .search(effectiveAgentId, effectiveStatus, categoryId, featured, blankToNull(search), pageable)
                .map(tripMergeService::toMergedResponse);
    }

    @Transactional
    public AgentTripDtos.AgentTripResponse copyTrip(Long tripId, User actor) {
        AgentTrip source = findEntity(tripId);
        tripAccessService.requireTripOwnerOrAdmin(actor, source);

        User targetAgent = actor.getRole() == Role.ADMIN ? source.getAgent() : actor;
        if (actor.getRole() == Role.ADMIN && source.getAgent() != null) {
            targetAgent = source.getAgent();
        } else if (actor.getRole() == Role.AGENT) {
            targetAgent = actor;
        }

        AgentTrip copy = tripCopyService.copyAgentTrip(source, targetAgent);
        return tripMergeService.toMergedResponse(agentTripRepository.save(copy));
    }

    private User resolveAgentTarget(User actor, Long agentId) {
        if (actor.getRole() == Role.AGENT) {
            if (agentId != null && !agentId.equals(actor.getId())) {
                throw new ValidationException("Agents can only create trips for themselves");
            }
            return actor;
        }
        // admin
        if (agentId == null) {
            throw new ValidationException("agentId is required when admin creates a trip");
        }
        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", agentId));
        if (agent.getRole() != Role.AGENT && agent.getRole() != Role.ADMIN) {
            throw new ValidationException("Target user must be an AGENT");
        }
        return agent;
    }

    private AgentTrip findEntity(Long id) {
        return agentTripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agent trip", id));
    }

    private void validateCustomRequest(AgentTripDtos.AgentTripRequest request) {
        if (request == null || request.getTitle() == null || request.getTitle().isBlank()) {
            throw new ValidationException("Trip title is required");
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
