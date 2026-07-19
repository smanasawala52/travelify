package com.travelify.service;

import com.travelify.dto.TripCategoryDtos;
import com.travelify.exception.ResourceConflictException;
import com.travelify.exception.ResourceNotFoundException;
import com.travelify.exception.ValidationException;
import com.travelify.model.TripCategory;
import com.travelify.model.User;
import com.travelify.repository.TripCategoryRepository;
import com.travelify.util.SlugUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing trip categories.
 */
@Service
public class TripCategoryService {

    private final TripCategoryRepository tripCategoryRepository;
    private final TripAccessService tripAccessService;

    public TripCategoryService(TripCategoryRepository tripCategoryRepository,
                              TripAccessService tripAccessService) {
        this.tripCategoryRepository = tripCategoryRepository;
        this.tripAccessService = tripAccessService;
    }

    @Transactional
    public TripCategoryDtos.TripCategoryResponse create(TripCategoryDtos.TripCategoryRequest request,
                                                          User actor) {
        tripAccessService.requireAdmin(actor);
        validateRequest(request);

        String slug = resolveUniqueSlug(request.getSlug(), request.getName(), null);
        
        TripCategory entity = TripCategory.builder()
                .name(request.getName().trim())
                .slug(slug)
                .description(request.getDescription())
                .icon(request.getIcon())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        return toResponse(tripCategoryRepository.save(entity));
    }

    @Transactional
    public TripCategoryDtos.TripCategoryResponse update(Long categoryId,
                                                          TripCategoryDtos.TripCategoryRequest request,
                                                          User actor) {
        tripAccessService.requireAdmin(actor);
        validateRequest(request);

        TripCategory entity = findEntity(categoryId);
        entity.setName(request.getName().trim());
        entity.setSlug(resolveUniqueSlug(request.getSlug(), request.getName(), categoryId));
        entity.setDescription(request.getDescription());
        entity.setIcon(request.getIcon());
        if (request.getSortOrder() != null) {
            entity.setSortOrder(request.getSortOrder());
        }
        if (request.getIsActive() != null) {
            entity.setIsActive(request.getIsActive());
        }
        
        return toResponse(entity);
    }

    @Transactional
    public void delete(Long categoryId, User actor) {
        tripAccessService.requireAdmin(actor);
        TripCategory entity = findEntity(categoryId);
        tripCategoryRepository.delete(entity);
    }

    @Transactional(readOnly = true)
    public TripCategoryDtos.TripCategoryResponse getById(Long categoryId) {
        return toResponse(findEntity(categoryId));
    }

    @Transactional(readOnly = true)
    public List<TripCategoryDtos.TripCategoryResponse> listAll() {
        return tripCategoryRepository.findByIsActiveTrue()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TripCategoryDtos.TripCategoryResponse> listAllIncludingInactive() {
        return tripCategoryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private TripCategory findEntity(Long id) {
        return tripCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip category", id));
    }

    private String resolveUniqueSlug(String requestedSlug, String name, Long excludeId) {
        String base = (requestedSlug != null && !requestedSlug.isBlank())
                ? SlugUtils.slugify(requestedSlug)
                : SlugUtils.slugify(name);
        
        String unique = SlugUtils.uniqueSlug(base, candidate -> excludeId == null
                ? tripCategoryRepository.existsBySlug(candidate)
                : tripCategoryRepository.existsBySlugAndIdNot(candidate, excludeId));
        
        if (excludeId == null && tripCategoryRepository.existsBySlug(unique)
                || excludeId != null && tripCategoryRepository.existsBySlugAndIdNot(unique, excludeId)) {
            throw new ResourceConflictException("Category slug already exists: " + unique);
        }
        return unique;
    }

    private void validateRequest(TripCategoryDtos.TripCategoryRequest request) {
        if (request == null || request.getName() == null || request.getName().isBlank()) {
            throw new ValidationException("Category name is required");
        }
        if (request.getName().length() > 100) {
            throw new ValidationException("Category name must be 100 characters or less");
        }
        if (request.getSlug() != null && request.getSlug().length() > 100) {
            throw new ValidationException("Category slug must be 100 characters or less");
        }
    }

    private TripCategoryDtos.TripCategoryResponse toResponse(TripCategory entity) {
        return TripCategoryDtos.TripCategoryResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .slug(entity.getSlug())
                .description(entity.getDescription())
                .icon(entity.getIcon())
                .sortOrder(entity.getSortOrder())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
