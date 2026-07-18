package com.travelify.repository;

import com.travelify.model.Booking;
import com.travelify.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByCustomer(User customer);
    List<Booking> findByCustomerId(Long customerId);
}