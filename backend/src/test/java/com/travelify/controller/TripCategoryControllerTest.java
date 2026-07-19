package com.travelify.controller;

import com.travelify.IntegrationTestBase;
import com.travelify.dto.TripCategoryDtos;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for TripCategoryController.
 */
class TripCategoryControllerTest extends IntegrationTestBase {

    @Test
    void listCategories_shouldReturnAllActiveCategories() throws Exception {
        mockMvc.perform(get("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(greaterThanOrEqualTo(5)));
    }

    @Test
    void getCategoryById_shouldReturnCategory() throws Exception {
        mockMvc.perform(get("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Adventure"));
    }

    @Test
    void getCategoryBySlug_shouldReturnCategory() throws Exception {
        mockMvc.perform(get("/api/categories/slug/adventure")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createCategory_shouldCreateNewCategory() throws Exception {
        TripCategoryDtos.TripCategoryRequest request = new TripCategoryDtos.TripCategoryRequest();
        request.setName("Luxury");
        request.setSlug("luxury");
        request.setDescription("Luxury travel experiences");

        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + getAdminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Luxury"));
    }

    @Test
    void createCategory_shouldFailForNonAdmin() throws Exception {
        TripCategoryDtos.TripCategoryRequest request = new TripCategoryDtos.TripCategoryRequest();
        request.setName("Unauthorized Category");
        request.setSlug("unauthorized-category");

        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + getAgentToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createCategory_shouldFailWithoutAuthentication() throws Exception {
        TripCategoryDtos.TripCategoryRequest request = new TripCategoryDtos.TripCategoryRequest();
        request.setName("No Auth Category");
        request.setSlug("no-auth-category");

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isUnauthorized());
    }
}
