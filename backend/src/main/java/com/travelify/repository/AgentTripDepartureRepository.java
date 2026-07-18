package com.travelify.repository;

import com.travelify.model.AgentTripDeparture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AgentTripDepartureRepository extends JpaRepository<AgentTripDeparture, Long> {

    List<AgentTripDeparture> findByAgentTripIdOrderByDepartureDateAsc(Long agentTripId);

    List<AgentTripDeparture> findByAgentTripIdAndIsCancelledFalseOrderByDepartureDateAsc(Long agentTripId);

    List<AgentTripDeparture> findByDepartureDateBetweenAndIsCancelledFalse(LocalDate from, LocalDate to);

    @Query("""
            SELECT d FROM AgentTripDeparture d
            WHERE d.agentTrip.id = :agentTripId
              AND d.isCancelled = false
              AND d.departureDate >= :from
              AND (d.availableSeats IS NULL OR d.availableSeats > 0)
            ORDER BY d.departureDate ASC
            """)
    List<AgentTripDeparture> findAvailableUpcoming(
            @Param("agentTripId") Long agentTripId,
            @Param("from") LocalDate from
    );

    void deleteByAgentTripId(Long agentTripId);
}
