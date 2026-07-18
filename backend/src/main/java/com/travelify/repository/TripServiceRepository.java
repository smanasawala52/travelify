package com.travelify.repository;

import com.travelify.model.ServiceType;
import com.travelify.model.TripService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TripServiceRepository extends JpaRepository<TripService, Long> {

    List<TripService> findByAgentTripId(Long agentTripId);

    List<TripService> findByServiceId(Long serviceId);

    Optional<TripService> findByAgentTripIdAndServiceId(Long agentTripId, Long serviceId);

    boolean existsByAgentTripIdAndServiceId(Long agentTripId, Long serviceId);

    List<TripService> findByAgentTripIdAndIsOptional(Long agentTripId, Boolean isOptional);

    @Query("""
            SELECT ts FROM TripService ts
            JOIN FETCH ts.service s
            WHERE ts.agentTrip.id = :agentTripId
              AND (:serviceType IS NULL OR s.serviceType = :serviceType)
            """)
    List<TripService> findByAgentTripIdAndServiceType(
            @Param("agentTripId") Long agentTripId,
            @Param("serviceType") ServiceType serviceType
    );

    void deleteByAgentTripId(Long agentTripId);

    void deleteByAgentTripIdAndServiceId(Long agentTripId, Long serviceId);

    long countByAgentTripId(Long agentTripId);
}
