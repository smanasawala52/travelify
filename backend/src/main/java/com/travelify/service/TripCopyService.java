package com.travelify.service;

import com.travelify.dto.AgentTripDtos;
import com.travelify.model.*;
import com.travelify.repository.AgentTripRepository;
import com.travelify.repository.TripCategoryRepository;
import com.travelify.util.SlugUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copies template scalars (and optional child collections from requests)
 * into new or existing {@link AgentTrip} entities.
 * <p>
 * Note: {@code trip_templates} only store scalar fields; itinerary / pricing /
 * images live on agent trips and are supplied via create/override requests.
 */
@Service
public class TripCopyService {

    private final AgentTripRepository agentTripRepository;
    private final TripCategoryRepository tripCategoryRepository;

    public TripCopyService(AgentTripRepository agentTripRepository,
                           TripCategoryRepository tripCategoryRepository) {
        this.agentTripRepository = agentTripRepository;
        this.tripCategoryRepository = tripCategoryRepository;
    }

    /**
     * Build a new agent trip from a published/admin template, applying optional overrides.
     */
    public AgentTrip copyFromTemplate(TripTemplate template, User agent, AgentTripDtos.OverrideRequest overrides) {
        Map<String, Boolean> overrideFlags = new HashMap<>();

        String title = template.getTitle();
        String slugBase = SlugUtils.slugify(template.getSlug() != null ? template.getSlug() : template.getTitle());
        String shortDescription = template.getShortDescription();
        String fullDescription = template.getFullDescription();
        String featuredImage = template.getFeaturedImage();
        TripCategory category = template.getCategory();
        String difficulty = template.getDifficulty();
        Integer durationDays = template.getDurationDays();
        Integer minAge = template.getMinAge();
        Integer maxGroupSize = template.getMaxGroupSize();
        Boolean isFeatured = Boolean.FALSE;
        PublishStatus status = PublishStatus.DRAFT;

        if (overrides != null) {
            if (overrides.getTitle() != null) {
                title = overrides.getTitle();
                overrideFlags.put("title", true);
            }
            if (overrides.getSlug() != null && !overrides.getSlug().isBlank()) {
                slugBase = overrides.getSlug().trim();
                overrideFlags.put("slug", true);
            } else if (overrides.getTitle() != null) {
                slugBase = title;
            }
            if (overrides.getShortDescription() != null) {
                shortDescription = overrides.getShortDescription();
                overrideFlags.put("shortDescription", true);
            }
            if (overrides.getFullDescription() != null) {
                fullDescription = overrides.getFullDescription();
                overrideFlags.put("fullDescription", true);
            }
            if (overrides.getFeaturedImage() != null) {
                featuredImage = overrides.getFeaturedImage();
                overrideFlags.put("featuredImage", true);
            }
            if (overrides.getCategoryId() != null) {
                category = resolveCategory(overrides.getCategoryId());
                overrideFlags.put("categoryId", true);
            }
            if (overrides.getDifficulty() != null) {
                difficulty = overrides.getDifficulty();
                overrideFlags.put("difficulty", true);
            }
            if (overrides.getDurationDays() != null) {
                durationDays = overrides.getDurationDays();
                overrideFlags.put("durationDays", true);
            }
            if (overrides.getMinAge() != null) {
                minAge = overrides.getMinAge();
                overrideFlags.put("minAge", true);
            }
            if (overrides.getMaxGroupSize() != null) {
                maxGroupSize = overrides.getMaxGroupSize();
                overrideFlags.put("maxGroupSize", true);
            }
            if (overrides.getIsFeatured() != null) {
                isFeatured = overrides.getIsFeatured();
                overrideFlags.put("isFeatured", true);
            }
            if (overrides.getStatus() != null) {
                status = overrides.getStatus();
                overrideFlags.put("status", true);
            }
        }

        String slug = uniqueAgentSlug(agent.getId(), SlugUtils.slugify(slugBase), null);

        AgentTrip trip = AgentTrip.builder()
                .template(template)
                .agent(agent)
                .title(title)
                .slug(slug)
                .shortDescription(shortDescription)
                .fullDescription(fullDescription)
                .featuredImage(featuredImage)
                .category(category)
                .difficulty(difficulty)
                .durationDays(durationDays)
                .minAge(minAge)
                .maxGroupSize(maxGroupSize)
                .isFeatured(isFeatured)
                .status(status)
                .overrideFields(overrideFlags)
                .build();

        if (overrides != null) {
            if (overrides.getItinerary() != null) {
                replaceItinerary(trip, overrides.getItinerary());
                overrideFlags.put("itinerary", true);
            }
            if (overrides.getPricing() != null) {
                replacePricing(trip, overrides.getPricing());
                overrideFlags.put("pricing", true);
            }
            if (overrides.getDepartures() != null) {
                replaceDepartures(trip, overrides.getDepartures());
                overrideFlags.put("departures", true);
            }
            if (overrides.getImages() != null) {
                replaceImages(trip, overrides.getImages());
                overrideFlags.put("images", true);
            }
        }

        return trip;
    }

    /**
     * Deep-clone an existing agent trip (children + optional new agent owner).
     */
    public AgentTrip copyAgentTrip(AgentTrip source, User targetAgent) {
        String slug = uniqueAgentSlug(
                targetAgent.getId(),
                SlugUtils.slugify(source.getSlug() + "-copy"),
                null
        );

        AgentTrip copy = AgentTrip.builder()
                .template(source.getTemplate())
                .agent(targetAgent)
                .title(source.getTitle() + " (Copy)")
                .slug(slug)
                .shortDescription(source.getShortDescription())
                .fullDescription(source.getFullDescription())
                .featuredImage(source.getFeaturedImage())
                .category(source.getCategory())
                .difficulty(source.getDifficulty())
                .durationDays(source.getDurationDays())
                .minAge(source.getMinAge())
                .maxGroupSize(source.getMaxGroupSize())
                .isFeatured(false)
                .status(PublishStatus.DRAFT)
                .overrideFields(source.getOverrideFields() == null
                        ? new HashMap<>()
                        : new HashMap<>(source.getOverrideFields()))
                .build();

        for (AgentTripItinerary day : source.getItinerary()) {
            copy.addItineraryDay(AgentTripItinerary.builder()
                    .dayNumber(day.getDayNumber())
                    .title(day.getTitle())
                    .description(day.getDescription())
                    .activities(day.getActivities())
                    .accommodation(day.getAccommodation())
                    .meals(day.getMeals())
                    .build());
        }
        for (AgentTripPricing row : source.getPricing()) {
            copy.addPricing(AgentTripPricing.builder()
                    .pricingType(row.getPricingType())
                    .price(row.getPrice())
                    .currency(row.getCurrency())
                    .adultPrice(row.getAdultPrice())
                    .childPrice(row.getChildPrice())
                    .infantPrice(row.getInfantPrice())
                    .minParticipants(row.getMinParticipants())
                    .maxParticipants(row.getMaxParticipants())
                    .build());
        }
        for (AgentTripDeparture dep : source.getDepartures()) {
            copy.addDeparture(AgentTripDeparture.builder()
                    .departureDate(dep.getDepartureDate())
                    .endDate(dep.getEndDate())
                    .availableSeats(dep.getAvailableSeats())
                    .priceOverride(dep.getPriceOverride())
                    .isCancelled(Boolean.TRUE.equals(dep.getIsCancelled()))
                    .build());
        }
        for (AgentTripImage image : source.getImages()) {
            copy.addImage(AgentTripImage.builder()
                    .imageUrl(image.getImageUrl())
                    .isFeatured(Boolean.TRUE.equals(image.getIsFeatured()))
                    .sortOrder(image.getSortOrder() == null ? 0 : image.getSortOrder())
                    .build());
        }

        return copy;
    }

    public void applyCustomRequest(AgentTrip trip, AgentTripDtos.AgentTripRequest request, boolean trackOverrides) {
        Map<String, Boolean> flags = trip.getOverrideFields() == null
                ? new HashMap<>()
                : new HashMap<>(trip.getOverrideFields());

        if (request.getTitle() != null) {
            trip.setTitle(request.getTitle());
            if (trackOverrides) {
                flags.put("title", true);
            }
        }
        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            trip.setSlug(uniqueAgentSlug(trip.getAgent().getId(), SlugUtils.slugify(request.getSlug()), trip.getId()));
            if (trackOverrides) {
                flags.put("slug", true);
            }
        }
        if (request.getShortDescription() != null) {
            trip.setShortDescription(request.getShortDescription());
            if (trackOverrides) {
                flags.put("shortDescription", true);
            }
        }
        if (request.getFullDescription() != null) {
            trip.setFullDescription(request.getFullDescription());
            if (trackOverrides) {
                flags.put("fullDescription", true);
            }
        }
        if (request.getFeaturedImage() != null) {
            trip.setFeaturedImage(request.getFeaturedImage());
            if (trackOverrides) {
                flags.put("featuredImage", true);
            }
        }
        if (request.getCategoryId() != null) {
            trip.setCategory(resolveCategory(request.getCategoryId()));
            if (trackOverrides) {
                flags.put("categoryId", true);
            }
        }
        if (request.getDifficulty() != null) {
            trip.setDifficulty(request.getDifficulty());
            if (trackOverrides) {
                flags.put("difficulty", true);
            }
        }
        if (request.getDurationDays() != null) {
            trip.setDurationDays(request.getDurationDays());
            if (trackOverrides) {
                flags.put("durationDays", true);
            }
        }
        if (request.getMinAge() != null) {
            trip.setMinAge(request.getMinAge());
            if (trackOverrides) {
                flags.put("minAge", true);
            }
        }
        if (request.getMaxGroupSize() != null) {
            trip.setMaxGroupSize(request.getMaxGroupSize());
            if (trackOverrides) {
                flags.put("maxGroupSize", true);
            }
        }
        if (request.getIsFeatured() != null) {
            trip.setIsFeatured(request.getIsFeatured());
            if (trackOverrides) {
                flags.put("isFeatured", true);
            }
        }
        if (request.getStatus() != null) {
            trip.setStatus(request.getStatus());
            if (trackOverrides) {
                flags.put("status", true);
            }
        }

        if (request.getItinerary() != null) {
            replaceItinerary(trip, request.getItinerary());
            if (trackOverrides) {
                flags.put("itinerary", true);
            }
        }
        if (request.getPricing() != null) {
            replacePricing(trip, request.getPricing());
            if (trackOverrides) {
                flags.put("pricing", true);
            }
        }
        if (request.getDepartures() != null) {
            replaceDepartures(trip, request.getDepartures());
            if (trackOverrides) {
                flags.put("departures", true);
            }
        }
        if (request.getImages() != null) {
            replaceImages(trip, request.getImages());
            if (trackOverrides) {
                flags.put("images", true);
            }
        }

        trip.setOverrideFields(flags);
    }

    public void replaceItinerary(AgentTrip trip, List<AgentTripDtos.ItineraryDayRequest> days) {
        trip.getItinerary().clear();
        if (days == null) {
            return;
        }
        for (AgentTripDtos.ItineraryDayRequest day : days) {
            trip.addItineraryDay(AgentTripItinerary.builder()
                    .dayNumber(day.getDayNumber())
                    .title(day.getTitle())
                    .description(day.getDescription())
                    .activities(day.getActivities())
                    .accommodation(day.getAccommodation())
                    .meals(day.getMeals())
                    .build());
        }
    }

    public void replacePricing(AgentTrip trip, List<AgentTripDtos.PricingRequest> rows) {
        trip.getPricing().clear();
        if (rows == null) {
            return;
        }
        for (AgentTripDtos.PricingRequest row : rows) {
            trip.addPricing(AgentTripPricing.builder()
                    .pricingType(row.getPricingType())
                    .price(row.getPrice())
                    .currency(row.getCurrency() == null ? "USD" : row.getCurrency())
                    .adultPrice(row.getAdultPrice())
                    .childPrice(row.getChildPrice())
                    .infantPrice(row.getInfantPrice())
                    .minParticipants(row.getMinParticipants())
                    .maxParticipants(row.getMaxParticipants())
                    .build());
        }
    }

    public void replaceDepartures(AgentTrip trip, List<AgentTripDtos.DepartureRequest> rows) {
        trip.getDepartures().clear();
        if (rows == null) {
            return;
        }
        for (AgentTripDtos.DepartureRequest row : rows) {
            trip.addDeparture(AgentTripDeparture.builder()
                    .departureDate(row.getDepartureDate())
                    .endDate(row.getEndDate())
                    .availableSeats(row.getAvailableSeats())
                    .priceOverride(row.getPriceOverride())
                    .isCancelled(Boolean.TRUE.equals(row.getIsCancelled()))
                    .build());
        }
    }

    public void replaceImages(AgentTrip trip, List<AgentTripDtos.ImageRequest> rows) {
        trip.getImages().clear();
        if (rows == null) {
            return;
        }
        int order = 0;
        for (AgentTripDtos.ImageRequest row : rows) {
            trip.addImage(AgentTripImage.builder()
                    .imageUrl(row.getImageUrl())
                    .isFeatured(Boolean.TRUE.equals(row.getIsFeatured()))
                    .sortOrder(row.getSortOrder() == null ? order : row.getSortOrder())
                    .build());
            order++;
        }
    }

    public String uniqueAgentSlug(Long agentId, String base, Long excludeTripId) {
        String normalized = base == null || base.isBlank() ? "trip" : SlugUtils.slugify(base);
        return uniqueAgentSlugInternal(agentId, normalized, excludeTripId);
    }

    private String uniqueAgentSlugInternal(Long agentId, String base, Long excludeTripId) {
        return uniqueAgentSlugInternal(agentId, base, excludeTripId, 0);
    }

    private String uniqueAgentSlugInternal(Long agentId, String base, Long excludeTripId, int depth) {
        if (depth > 10_000) {
            throw new IllegalStateException("Unable to generate unique agent slug");
        }
        String candidate = depth == 0 ? base : base + "-" + (depth + 1);
        // depth mapping: first try base, then base-2, base-3...
        if (depth == 0) {
            candidate = base;
        } else {
            candidate = base + "-" + (depth + 1);
        }
        boolean taken = excludeTripId == null
                ? agentTripRepository.existsByAgentIdAndSlug(agentId, candidate)
                : agentTripRepository.existsByAgentIdAndSlugAndIdNot(agentId, candidate, excludeTripId);
        if (!taken) {
            return candidate;
        }
        return uniqueAgentSlugInternal(agentId, base, excludeTripId, depth + 1);
    }

    private TripCategory resolveCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return tripCategoryRepository.findById(categoryId).orElse(null);
    }
}
