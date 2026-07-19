package com.travelify.controller;

import com.travelify.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for AgentTemplateController.
 * Tests read-only access to published templates for agents.
 */
class AgentTemplateControllerTest extends IntegrationTestBase {

    @Test
    void listTemplates_shouldReturnPublishedTemplates() throws Exception {
        mockMvc.perform(get("/api/agent/templates")
                        .header("Authorization", "Bearer " + getAgentToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(greaterThanOrEqualTo(4)))
                .andExpect(jsonPath("$.content[*].status", everyItem(equalTo("PUBLISHED"))));
    }

    @Test
    void getTemplateById_shouldReturnTemplate() throws Exception {
        mockMvc.perform(get("/api/agent/templates/1")
                        .header("Authorization", "Bearer " + getAgentToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Bali Adventure"));
    }

    @Test
    void getTemplateById_shouldReturn404ForDraftTemplate() throws Exception {
        // Template ID 3 (Rome Cultural) is in DRAFT status
        mockMvc.perform(get("/api/agent/templates/3")
                        .header("Authorization", "Bearer " + getAgentToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void listTemplates_shouldFailWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/agent/templates")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
