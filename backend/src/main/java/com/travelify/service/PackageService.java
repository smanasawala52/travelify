package com.travelify.service;

import com.travelify.dto.PackageDtos;
import com.travelify.exception.ApiException;
import com.travelify.model.TravelPackage;
import com.travelify.model.User;
import com.travelify.repository.TravelPackageRepository;
import com.travelify.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PackageService {
    private final TravelPackageRepository packageRepository;
    private final UserRepository userRepository;

    public PackageService(TravelPackageRepository packageRepository, UserRepository userRepository) {
        this.packageRepository = packageRepository;
        this.userRepository = userRepository;
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
                .build();
    }
}