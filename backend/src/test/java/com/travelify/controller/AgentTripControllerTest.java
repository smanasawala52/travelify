package com.travelify.controller;

import com.travelify.IntegrationTestBase;
import com.travelify.dto.AgentTripDtos;
import com.travelify.model.PricingType;
import com.travelify.model.PublishStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for AgentTripController.
 */
class AgentTripControllerTest extends IntegrationTestBase {

    @Test
    void listTrips_shouldReturnAgentTrips() throws Exception {
        mockMvc.perform(get("/api/agent/trips")
                        .header("Authorization", "Bearer " + getAgentToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(greaterThanOrEqualTo(3)));
    }

    @Test
    void getTripById_shouldReturnTrip() throws Exception {
        mockMvc.perform(get("/api/agent/trips/1")
                        .header("Authorization", "Bearer " + getAgentToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Bali Adventure - Agent Version"));
    }

    @Test
    void getTripById_shouldFailForUnauthorizedAgent() throws Exception {
        mockMvc.perform(get("/api/agent/trips/1")
                        .header("Authorization", "Bearer " + getHotelProviderToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void createCustomTrip_shouldCreateNewTrip() throws Exception {
        AgentTripDtos.AgentTripRequest request = new AgentTripDtos.AgentTripRequest();
        request.setTitle("New Custom Trip");
        request.setSlug("new-custom-trip");
        request.setCategoryId(1L);
        request.setDurationDays(7);

        mockMvc.perform(post("/api/agent/trips")
                        .header("Authorization", "Bearer " + getAgentToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("New Custom Trip"));
    }

    @Test
    void createCustomTrip_shouldFailWithoutAuthentication() throws Exception {
        AgentTripDtos.AgentTripRequest request = new AgentTripDtos.AgentTripRequest();
        request.setTitle("No Auth Trip");
        request.setSlug("no-auth-trip");
        request.setCategoryId(1L);

        mockMvc.perform(post("/api/agent/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isUnauthorized());
    }
}
