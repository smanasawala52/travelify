package com.travelify.repository;

import com.travelify.model.Booking;
import com.travelify.model.Booking.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);
    List<Booking> findByUserIdAndStatus(Long userId, BookingStatus status);
    List<Booking> findByStatus(BookingStatus status);
    // Add other finders as needed for agent/admin views
}
