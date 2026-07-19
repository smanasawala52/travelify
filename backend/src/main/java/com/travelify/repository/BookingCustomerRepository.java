package com.travelify.repository;

import com.travelify.model.BookingCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingCustomerRepository extends JpaRepository<BookingCustomer, Long> {
}
