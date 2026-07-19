package com.travelify.service;

import com.travelify.exception.SeatsUnavailableException;
import com.travelify.model.AgentTripDeparture;
import com.travelify.repository.AgentTripDepartureRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final AgentTripDepartureRepository departureRepository;

    @Transactional
    public void reserveSeats(Long departureId, int quantity) {
        AgentTripDeparture departure = departureRepository.findById(departureId)
                .orElseThrow(() -> new SeatsUnavailableException("Departure not found."));

        if (departure.getAvailableSeats() == null || departure.getAvailableSeats() < quantity) {
            throw new SeatsUnavailableException("Not enough seats available for departure ID: " + departureId);
        }

        // Temporarily reduce available seats. This will be confirmed or released later.
        departure.setAvailableSeats(departure.getAvailableSeats() - quantity);
        departureRepository.save(departure);
    }

    @Transactional
    public void releaseSeats(Long departureId, int quantity) {
        AgentTripDeparture departure = departureRepository.findById(departureId)
                .orElseThrow(() -> new SeatsUnavailableException("Departure not found."));

        departure.setAvailableSeats(departure.getAvailableSeats() + quantity);
        departureRepository.save(departure);
    }

    @Transactional
    public void confirmSeats(Long departureId, int quantity) {
        // For now, reserveSeats already reduces the count.
        // This method can be used for a more complex two-phase commit if needed.
        // For simplicity, we assume reserveSeats already handles the reduction.
        // If a separate temporary reservation was implemented, this would make it permanent.
        // As per the prompt, "reduces available_seats in departure (if status changes to CONFIRMED later)"
        // the actual reduction happens on booking creation (PENDING) and is confirmed by payment.
        // So, this method might just be a no-op or for future expansion.
        // The current `reserveSeats` already reduces the count.
        // If we want a temporary reservation, `reserveSeats` would decrement a 'temp_reserved_seats'
        // and `confirmSeats` would move that to 'available_seats'.
        // For now, `reserveSeats` directly modifies `available_seats`.
        // So, this method is a placeholder or can be used to ensure consistency.
    }
}
