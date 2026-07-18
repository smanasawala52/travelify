package com.travelify.repository;

import com.travelify.model.AgentTripImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AgentTripImageRepository extends JpaRepository<AgentTripImage, Long> {

    List<AgentTripImage> findByAgentTripIdOrderBySortOrderAsc(Long agentTripId);

    Optional<AgentTripImage> findFirstByAgentTripIdAndIsFeaturedTrue(Long agentTripId);

    void deleteByAgentTripId(Long agentTripId);

    long countByAgentTripId(Long agentTripId);
}
