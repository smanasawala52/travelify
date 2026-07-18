package com.travelify.repository;

import com.travelify.model.AgentTripItinerary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AgentTripItineraryRepository extends JpaRepository<AgentTripItinerary, Long> {

    List<AgentTripItinerary> findByAgentTripIdOrderByDayNumberAsc(Long agentTripId);

    Optional<AgentTripItinerary> findByAgentTripIdAndDayNumber(Long agentTripId, Integer dayNumber);

    void deleteByAgentTripId(Long agentTripId);

    long countByAgentTripId(Long agentTripId);
}
