package com.travelify.service;

import com.travelify.dto.AgentTripDtos;
import com.travelify.model.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Merges template base data with agent overrides for display.
 * <p>
 * For scalar fields: if {@code override_fields[field] == true}, use agent value;
 * otherwise prefer template value when a template is linked.
 * Child collections always come from the agent trip (templates have no children).
 */
@Service
public class TripMergeService {

    public AgentTripDtos.AgentTripResponse toMergedResponse(AgentTrip trip) {
        TripTemplate template = trip.getTemplate();
        Map<String, Boolean> overrides = trip.getOverrideFields() == null
                ? Map.of()
                : trip.getOverrideFields();
        boolean hasTemplate = template != null;

        String title = pick("title", overrides, hasTemplate, trip.getTitle(),
                hasTemplate ? template.getTitle() : null);
        String shortDescription = pick("shortDescription", overrides, hasTemplate, trip.getShortDescription(),
                hasTemplate ? template.getShortDescription() : null);
        String fullDescription = pick("fullDescription", overrides, hasTemplate, trip.getFullDescription(),
                hasTemplate ? template.getFullDescription() : null);
        String featuredImage = pick("featuredImage", overrides, hasTemplate, trip.getFeaturedImage(),
                hasTemplate ? template.getFeaturedImage() : null);
        String difficulty = pick("difficulty", overrides, hasTemplate, trip.getDifficulty(),
                hasTemplate ? template.getDifficulty() : null);
        Integer durationDays = pick("durationDays", overrides, hasTemplate, trip.getDurationDays(),
                hasTemplate ? template.getDurationDays() : null);
        Integer minAge = pick("minAge", overrides, hasTemplate, trip.getMinAge(),
                hasTemplate ? template.getMinAge() : null);
        Integer maxGroupSize = pick("maxGroupSize", overrides, hasTemplate, trip.getMaxGroupSize(),
                hasTemplate ? template.getMaxGroupSize() : null);

        TripCategory category = resolveCategory(trip, template, overrides);
        Boolean isFeatured = Boolean.TRUE.equals(overrides.get("isFeatured"))
                ? trip.getIsFeatured()
                : (trip.getIsFeatured() != null ? trip.getIsFeatured() : Boolean.FALSE);
        PublishStatus status = trip.getStatus();

        return AgentTripDtos.AgentTripResponse.builder()
                .id(trip.getId())
                .templateId(template != null ? template.getId() : null)
                .templateTitle(template != null ? template.getTitle() : null)
                .agentId(trip.getAgent() != null ? trip.getAgent().getId() : null)
                .agentEmail(trip.getAgent() != null ? trip.getAgent().getEmail() : null)
                .title(title)
                .slug(trip.getSlug())
                .shortDescription(shortDescription)
                .fullDescription(fullDescription)
                .featuredImage(featuredImage)
                .categoryId(category != null ? category.getId() : null)
                .categoryName(category != null ? category.getName() : null)
                .difficulty(difficulty)
                .durationDays(durationDays)
                .minAge(minAge)
                .maxGroupSize(maxGroupSize)
                .isFeatured(isFeatured)
                .status(status)
                .overrideFields(new HashMap<>(overrides))
                .mergedFromTemplate(hasTemplate)
                .createdAt(trip.getCreatedAt())
                .updatedAt(trip.getUpdatedAt())
                .itinerary(mapItinerary(trip.getItinerary()))
                .pricing(mapPricing(trip.getPricing()))
                .departures(mapDepartures(trip.getDepartures()))
                .images(mapImages(trip.getImages()))
                .build();
    }

    private TripCategory resolveCategory(AgentTrip trip, TripTemplate template, Map<String, Boolean> overrides) {
        if (Boolean.TRUE.equals(overrides.get("categoryId")) || template == null) {
            return trip.getCategory();
        }
        return template.getCategory() != null ? template.getCategory() : trip.getCategory();
    }

    private <T> T pick(String field, Map<String, Boolean> overrides, boolean hasTemplate, T agentValue, T templateValue) {
        if (Boolean.TRUE.equals(overrides.get(field)) || !hasTemplate) {
            return agentValue;
        }
        return templateValue != null ? templateValue : agentValue;
    }

    private List<AgentTripDtos.ItineraryDayResponse> mapItinerary(List<AgentTripItinerary> days) {
        List<AgentTripDtos.ItineraryDayResponse> result = new ArrayList<>();
        if (days == null) {
            return result;
        }
        for (AgentTripItinerary day : days) {
            result.add(AgentTripDtos.ItineraryDayResponse.builder()
                    .id(day.getId())
                    .dayNumber(day.getDayNumber())
                    .title(day.getTitle())
                    .description(day.getDescription())
                    .activities(day.getActivities())
                    .accommodation(day.getAccommodation())
                    .meals(day.getMeals())
                    .build());
        }
        return result;
    }

    private List<AgentTripDtos.PricingResponse> mapPricing(List<AgentTripPricing> rows) {
        List<AgentTripDtos.PricingResponse> result = new ArrayList<>();
        if (rows == null) {
            return result;
        }
        for (AgentTripPricing row : rows) {
            result.add(AgentTripDtos.PricingResponse.builder()
                    .id(row.getId())
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
        return result;
    }

    private List<AgentTripDtos.DepartureResponse> mapDepartures(List<AgentTripDeparture> rows) {
        List<AgentTripDtos.DepartureResponse> result = new ArrayList<>();
        if (rows == null) {
            return result;
        }
        for (AgentTripDeparture row : rows) {
            result.add(AgentTripDtos.DepartureResponse.builder()
                    .id(row.getId())
                    .departureDate(row.getDepartureDate())
                    .endDate(row.getEndDate())
                    .availableSeats(row.getAvailableSeats())
                    .priceOverride(row.getPriceOverride())
                    .isCancelled(row.getIsCancelled())
                    .build());
        }
        return result;
    }

    private List<AgentTripDtos.ImageResponse> mapImages(List<AgentTripImage> rows) {
        List<AgentTripDtos.ImageResponse> result = new ArrayList<>();
        if (rows == null) {
            return result;
        }
        for (AgentTripImage row : rows) {
            result.add(AgentTripDtos.ImageResponse.builder()
                    .id(row.getId())
                    .imageUrl(row.getImageUrl())
                    .isFeatured(row.getIsFeatured())
                    .sortOrder(row.getSortOrder())
                    .build());
        }
        return result;
    }
}
