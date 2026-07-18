package com.travelify.service;

import com.travelify.dto.TripTemplateDtos;
import com.travelify.exception.ResourceConflictException;
import com.travelify.exception.ResourceInUseException;
import com.travelify.exception.ResourceNotFoundException;
import com.travelify.exception.ValidationException;
import com.travelify.model.PublishStatus;
import com.travelify.model.TripCategory;
import com.travelify.model.TripTemplate;
import com.travelify.model.User;
import com.travelify.repository.AgentTripRepository;
import com.travelify.repository.TripCategoryRepository;
import com.travelify.repository.TripTemplateRepository;
import com.travelify.util.SlugUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TripTemplateService {

    private final TripTemplateRepository tripTemplateRepository;
    private final TripCategoryRepository tripCategoryRepository;
    private final AgentTripRepository agentTripRepository;
    private final TripAccessService tripAccessService;

    public TripTemplateService(TripTemplateRepository tripTemplateRepository,
                               TripCategoryRepository tripCategoryRepository,
                               AgentTripRepository agentTripRepository,
                               TripAccessService tripAccessService) {
        this.tripTemplateRepository = tripTemplateRepository;
        this.tripCategoryRepository = tripCategoryRepository;
        this.agentTripRepository = agentTripRepository;
        this.tripAccessService = tripAccessService;
    }

    @Transactional
    public TripTemplateDtos.TripTemplateResponse createTemplate(User actor,
                                                                TripTemplateDtos.TripTemplateRequest request) {
        tripAccessService.requireAdmin(actor);
        validateRequest(request);

        String slug = resolveUniqueSlug(request.getSlug(), request.getTitle(), null);
        TripTemplate entity = TripTemplate.builder()
                .title(request.getTitle().trim())
                .slug(slug)
                .shortDescription(request.getShortDescription())
                .fullDescription(request.getFullDescription())
                .featuredImage(request.getFeaturedImage())
                .category(resolveCategory(request.getCategoryId()))
                .difficulty(request.getDifficulty())
                .durationDays(request.getDurationDays())
                .minAge(request.getMinAge())
                .maxGroupSize(request.getMaxGroupSize())
                .isFeatured(Boolean.TRUE.equals(request.getIsFeatured()))
                .status(request.getStatus() == null ? PublishStatus.DRAFT : request.getStatus())
                .createdBy(actor)
                .build();

        return toResponse(tripTemplateRepository.save(entity));
    }

    @Transactional
    public TripTemplateDtos.TripTemplateResponse updateTemplate(User actor,
                                                                Long id,
                                                                TripTemplateDtos.TripTemplateRequest request) {
        tripAccessService.requireAdmin(actor);
        validateRequest(request);

        TripTemplate entity = findEntity(id);
        entity.setTitle(request.getTitle().trim());
        entity.setSlug(resolveUniqueSlug(request.getSlug(), request.getTitle(), id));
        entity.setShortDescription(request.getShortDescription());
        entity.setFullDescription(request.getFullDescription());
        entity.setFeaturedImage(request.getFeaturedImage());
        entity.setCategory(resolveCategory(request.getCategoryId()));
        entity.setDifficulty(request.getDifficulty());
        entity.setDurationDays(request.getDurationDays());
        entity.setMinAge(request.getMinAge());
        entity.setMaxGroupSize(request.getMaxGroupSize());
        if (request.getIsFeatured() != null) {
            entity.setIsFeatured(request.getIsFeatured());
        }
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
        return toResponse(entity);
    }

    /**
     * Soft-deletes (archives) a template when no agent trips reference it.
     */
    @Transactional
    public TripTemplateDtos.TripTemplateResponse deleteTemplate(User actor, Long id) {
        tripAccessService.requireAdmin(actor);
        TripTemplate entity = findEntity(id);

        if (agentTripRepository.existsByTemplateId(id)) {
            throw new ResourceInUseException(
                    "Template is used by agent trips and cannot be archived until those trips are unlinked");
        }
        entity.setStatus(PublishStatus.ARCHIVED);
        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public TripTemplateDtos.TripTemplateResponse getTemplate(Long id) {
        return toResponse(findEntity(id));
    }

    @Transactional(readOnly = true)
    public Page<TripTemplateDtos.TripTemplateResponse> listTemplates(Pageable pageable,
                                                                     PublishStatus status,
                                                                     Long categoryId,
                                                                     Boolean featured,
                                                                     String search) {
        return tripTemplateRepository
                .search(status, categoryId, featured, blankToNull(search), pageable)
                .map(this::toResponse);
    }

    @Transactional
    public TripTemplateDtos.TripTemplateResponse duplicateTemplate(User actor, Long id) {
        tripAccessService.requireAdmin(actor);
        TripTemplate source = findEntity(id);

        String slug = resolveUniqueSlug(null, source.getTitle() + " copy", null);
        TripTemplate clone = TripTemplate.builder()
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
                .createdBy(actor)
                .build();

        return toResponse(tripTemplateRepository.save(clone));
    }

    private TripTemplate findEntity(Long id) {
        return tripTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip template", id));
    }

    private TripCategory resolveCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return tripCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip category", categoryId));
    }

    private String resolveUniqueSlug(String requestedSlug, String title, Long excludeId) {
        String base = (requestedSlug != null && !requestedSlug.isBlank())
                ? SlugUtils.slugify(requestedSlug)
                : SlugUtils.slugify(title);
        String unique = SlugUtils.uniqueSlug(base, candidate -> excludeId == null
                ? tripTemplateRepository.existsBySlug(candidate)
                : tripTemplateRepository.existsBySlugAndIdNot(candidate, excludeId));
        if (excludeId == null && tripTemplateRepository.existsBySlug(unique)
                || excludeId != null && tripTemplateRepository.existsBySlugAndIdNot(unique, excludeId)) {
            throw new ResourceConflictException("Template slug already exists: " + unique);
        }
        return unique;
    }

    private void validateRequest(TripTemplateDtos.TripTemplateRequest request) {
        if (request == null || request.getTitle() == null || request.getTitle().isBlank()) {
            throw new ValidationException("Template title is required");
        }
        if (request.getDurationDays() != null && request.getDurationDays() < 1) {
            throw new ValidationException("durationDays must be at least 1");
        }
        if (request.getMinAge() != null && request.getMinAge() < 0) {
            throw new ValidationException("minAge cannot be negative");
        }
        if (request.getMaxGroupSize() != null && request.getMaxGroupSize() < 1) {
            throw new ValidationException("maxGroupSize must be at least 1");
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private TripTemplateDtos.TripTemplateResponse toResponse(TripTemplate entity) {
        return TripTemplateDtos.TripTemplateResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .slug(entity.getSlug())
                .shortDescription(entity.getShortDescription())
                .fullDescription(entity.getFullDescription())
                .featuredImage(entity.getFeaturedImage())
                .categoryId(entity.getCategory() != null ? entity.getCategory().getId() : null)
                .categoryName(entity.getCategory() != null ? entity.getCategory().getName() : null)
                .difficulty(entity.getDifficulty())
                .durationDays(entity.getDurationDays())
                .minAge(entity.getMinAge())
                .maxGroupSize(entity.getMaxGroupSize())
                .isFeatured(entity.getIsFeatured())
                .status(entity.getStatus())
                .createdById(entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null)
                .createdByEmail(entity.getCreatedBy() != null ? entity.getCreatedBy().getEmail() : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
