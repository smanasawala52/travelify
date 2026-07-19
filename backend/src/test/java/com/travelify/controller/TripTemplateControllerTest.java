package com.travelify.controller;

import com.travelify.IntegrationTestBase;
import com.travelify.dto.TripTemplateDtos;
import com.travelify.model.PublishStatus;
import com.travelify.repository.TripTemplateRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for TripTemplateController.
 * Tests admin-only endpoints with JWT authentication.
 */
class TripTemplateControllerTest extends IntegrationTestBase {

    @Autowired
    private TripTemplateRepository tripTemplateRepository;

    @Test
    void listTemplates_shouldReturnAllTemplates() throws Exception {
        mockMvc.perform(get("/api/admin/templates")
                        .header("Authorization", "Bearer " + getAdminToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(greaterThanOrEqualTo(5)))
                .andExpect(jsonPath("$.content[0].title").isNotEmpty());
    }

    @Test
    void getTemplateById_shouldReturnTemplate() throws Exception {
        mockMvc.perform(get("/api/admin/templates/1")
                        .header("Authorization", "Bearer " + getAdminToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Bali Adventure"))
                .andExpect(jsonPath("$.slug").value("bali-adventure"))
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    @Test
    void createTemplate_shouldCreateNewTemplate() throws Exception {
        TripTemplateDtos.TripTemplateRequest request = new TripTemplateDtos.TripTemplateRequest();
        request.setTitle("New Test Template");
        request.setSlug("new-test-template");
        request.setCategoryId(1L);
        request.setDurationDays(5);
        request.setDifficulty("EASY");
        request.setStatus(PublishStatus.DRAFT);

        mockMvc.perform(post("/api/admin/templates")
                        .header("Authorization", "Bearer " + getAdminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("New Test Template"));

        assertThat(tripTemplateRepository.findBySlug("new-test-template")).isPresent();
    }

    @Test
    void createTemplate_shouldFailForNonAdmin() throws Exception {
        TripTemplateDtos.TripTemplateRequest request = new TripTemplateDtos.TripTemplateRequest();
        request.setTitle("Unauthorized Template");
        request.setSlug("unauthorized-template");
        request.setCategoryId(1L);
        request.setDurationDays(5);

        mockMvc.perform(post("/api/admin/templates")
                        .header("Authorization", "Bearer " + getAgentToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createTemplate_shouldFailWithoutAuthentication() throws Exception {
        TripTemplateDtos.TripTemplateRequest request = new TripTemplateDtos.TripTemplateRequest();
        request.setTitle("No Auth Template");
        request.setSlug("no-auth-template");
        request.setCategoryId(1L);
        request.setDurationDays(5);

        mockMvc.perform(post("/api/admin/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isUnauthorized());
    }
}
