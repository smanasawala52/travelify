package com.travelify.repository;

import com.travelify.model.BookingAddon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingAddonRepository extends JpaRepository<BookingAddon, Long> {
}
