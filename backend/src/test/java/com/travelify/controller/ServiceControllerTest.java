package com.travelify.controller;

import com.travelify.IntegrationTestBase;
import com.travelify.dto.ServiceDtos;
import com.travelify.model.PublishStatus;
import com.travelify.model.ServiceType;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for ServiceController.
 */
class ServiceControllerTest extends IntegrationTestBase {

    @Test
    void listMyServices_shouldReturnProviderServices() throws Exception {
        mockMvc.perform(get("/api/provider/services/my")
                        .header("Authorization", "Bearer " + getHotelProviderToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(greaterThanOrEqualTo(5)));
    }

    @Test
    void getServiceById_shouldReturnService() throws Exception {
        mockMvc.perform(get("/api/provider/services/1")
                        .header("Authorization", "Bearer " + getHotelProviderToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Deluxe Ocean View"));
    }

    @Test
    void getServiceById_shouldFailForUnauthorizedProvider() throws Exception {
        mockMvc.perform(get("/api/provider/services/1")
                        .header("Authorization", "Bearer " + getInsuranceProviderToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void createService_shouldCreateNewService() throws Exception {
        ServiceDtos.ServiceRequest request = new ServiceDtos.ServiceRequest();
        request.setServiceType(ServiceType.HOTEL_ROOM);
        request.setName("New Luxury Suite");
        request.setPrice(new BigDecimal("1200.00"));
        request.setStatus(PublishStatus.PUBLISHED);

        mockMvc.perform(post("/api/provider/services")
                        .header("Authorization", "Bearer " + getHotelProviderToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("New Luxury Suite"));
    }

    @Test
    void createService_shouldFailWithoutAuthentication() throws Exception {
        ServiceDtos.ServiceRequest request = new ServiceDtos.ServiceRequest();
        request.setServiceType(ServiceType.HOTEL_ROOM);
        request.setName("No Auth Service");
        request.setPrice(new BigDecimal("100.00"));

        mockMvc.perform(post("/api/provider/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateService_shouldUpdateExistingService() throws Exception {
        ServiceDtos.ServiceRequest request = new ServiceDtos.ServiceRequest();
        request.setServiceType(ServiceType.HOTEL_ROOM);
        request.setName("Updated Deluxe Ocean View");
        request.setPrice(new BigDecimal("275.00"));

        mockMvc.perform(put("/api/provider/services/1")
                        .header("Authorization", "Bearer " + getHotelProviderToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Deluxe Ocean View"));
    }

    @Test
    void customer_shouldNotAccessServiceManagement() throws Exception {
        mockMvc.perform(get("/api/provider/services/my")
                        .header("Authorization", "Bearer " + getCustomerToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
