package com.travelify.service;

import com.travelify.dto.PackageDtos;
import com.travelify.exception.ApiException;
import com.travelify.model.TravelPackage;
import com.travelify.model.User;
import com.travelify.repository.TravelPackageRepository;
import com.travelify.repository.TripCategoryRepository;
import com.travelify.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class PackageService {
    private final TravelPackageRepository packageRepository;
    private final UserRepository userRepository;
    private final TripCategoryRepository tripCategoryRepository;

    public PackageService(TravelPackageRepository packageRepository, UserRepository userRepository, TripCategoryRepository tripCategoryRepository) {
        this.packageRepository = packageRepository;
        this.userRepository = userRepository;
        this.tripCategoryRepository = tripCategoryRepository;
    }

    public List<PackageDtos.PackageResponse> listActive() {
        return packageRepository.findByActiveTrue().stream().map(this::toResponse).toList();
    }

    public List<PackageDtos.PackageResponse> listAll() {
        return packageRepository.findAll().stream().map(this::toResponse).toList();
    }

    public PackageDtos.PackageResponse getById(Long id) {
        return toResponse(find(id));
    }

    public List<PackageDtos.PackageResponse> listFilteredPackages(PackageDtos.PackageFilterRequest filterRequest) {
        Specification<TravelPackage> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isTrue(root.get("active"))); // Only active packages

            if (filterRequest.getCategorySlug() != null && !filterRequest.getCategorySlug().isEmpty()) {
                predicates.add(cb.equal(root.get("tripCategory").get("slug"), filterRequest.getCategorySlug()));
            }
            if (filterRequest.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filterRequest.getMinPrice()));
            }
            if (filterRequest.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), filterRequest.getMaxPrice()));
            }
            if (filterRequest.getMinDurationDays() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("durationDays"), filterRequest.getMinDurationDays()));
            }
            if (filterRequest.getMaxDurationDays() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("durationDays"), filterRequest.getMaxDurationDays()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return packageRepository.findAll(spec).stream().map(this::toResponse).toList();
    }

    @Transactional
    public PackageDtos.PackageResponse create(PackageDtos.PackageRequest request, String agentEmail) {
        User agent = userRepository.findByEmail(agentEmail)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        TravelPackage entity = TravelPackage.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .destination(request.getDestination())
                .price(request.getPrice())
                .durationDays(request.getDurationDays())
                .active(request.getActive() == null || request.getActive())
                .createdBy(agent)
                .build();
        return toResponse(packageRepository.save(entity));
    }

    @Transactional
    public PackageDtos.PackageResponse update(Long id, PackageDtos.PackageRequest request) {
        TravelPackage entity = find(id);
        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setDestination(request.getDestination());
        entity.setPrice(request.getPrice());
        entity.setDurationDays(request.getDurationDays());
        if (request.getActive() != null) {
            entity.setActive(request.getActive());
        }
        return toResponse(entity);
    }

    private TravelPackage find(Long id) {
        return packageRepository.findById(id)
                .orElseThrow(() -> new ApiException("Package not found", HttpStatus.NOT_FOUND));
    }

    private PackageDtos.PackageResponse toResponse(TravelPackage entity) {
        return PackageDtos.PackageResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .destination(entity.getDestination())
                .price(entity.getPrice())
                .durationDays(entity.getDurationDays())
                .active(entity.getActive())
                .categoryName(entity.getTripCategory() != null ? entity.getTripCategory().getName() : null)
                .categorySlug(entity.getTripCategory() != null ? entity.getTripCategory().getSlug() : null)
                .build();
    }
}
