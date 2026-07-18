package com.travelify.repository;

import com.travelify.model.AgentTripPricing;
import com.travelify.model.PricingType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AgentTripPricingRepository extends JpaRepository<AgentTripPricing, Long> {

    List<AgentTripPricing> findByAgentTripId(Long agentTripId);

    List<AgentTripPricing> findByAgentTripIdAndPricingType(Long agentTripId, PricingType pricingType);

    void deleteByAgentTripId(Long agentTripId);
}
