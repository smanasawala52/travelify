package com.travelify.controller;

import com.travelify.dto.TripTemplateDtos;
import com.travelify.model.PublishStatus;
import com.travelify.service.TripTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for agents to browse published trip templates.
 * Read-only access to templates for agent trip creation.
 */
@RestController
@RequestMapping("/api/agent/templates")
@Tag(name = "Agent Templates", description = "Published trip templates for agents")
public class AgentTemplateController {

    private final TripTemplateService tripTemplateService;

    public AgentTemplateController(TripTemplateService tripTemplateService) {
        this.tripTemplateService = tripTemplateService;
    }

    @GetMapping("/{templateId}")
    @Operation(summary = "Get a single published template by ID")
    public TripTemplateDtos.TripTemplateResponse getById(@PathVariable Long templateId) {
        return tripTemplateService.getTemplate(templateId);
    }

    @GetMapping
    @Operation(summary = "List published trip templates")
    public Page<TripTemplateDtos.TripTemplateResponse> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false) String search,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        // Agents can only see PUBLISHED templates
        PublishStatus statusEnum = status != null ? PublishStatus.valueOf(status) : PublishStatus.PUBLISHED;
        return tripTemplateService.listTemplates(pageable, statusEnum, categoryId, featured, search);
    }
}
