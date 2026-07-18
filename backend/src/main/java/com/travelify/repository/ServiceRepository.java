package com.travelify.repository;

import com.travelify.model.PublishStatus;
import com.travelify.model.Service;
import com.travelify.model.ServiceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ServiceRepository extends JpaRepository<Service, Long> {

    List<Service> findByProviderId(Long providerId);

    List<Service> findByProviderIdAndStatus(Long providerId, PublishStatus status);

    List<Service> findByProviderIdAndServiceType(Long providerId, ServiceType serviceType);

    List<Service> findByProviderIdAndServiceTypeAndStatus(
            Long providerId,
            ServiceType serviceType,
            PublishStatus status
    );

    List<Service> findByServiceTypeAndStatus(ServiceType serviceType, PublishStatus status);

    List<Service> findByStatus(PublishStatus status);

    Page<Service> findByProviderId(Long providerId, Pageable pageable);

    Page<Service> findByServiceTypeAndStatus(ServiceType serviceType, PublishStatus status, Pageable pageable);

    @Query("""
            SELECT s FROM Service s
            WHERE (:providerId IS NULL OR s.provider.id = :providerId)
              AND (:serviceType IS NULL OR s.serviceType = :serviceType)
              AND (:status IS NULL OR s.status = :status)
              AND (
                    :search IS NULL OR :search = ''
                    OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """)
    Page<Service> search(
            @Param("providerId") Long providerId,
            @Param("serviceType") ServiceType serviceType,
            @Param("status") PublishStatus status,
            @Param("search") String search,
            Pageable pageable
    );
}
