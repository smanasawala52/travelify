package com.travelify.repository;

import com.travelify.model.TravelPackage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TravelPackageRepository extends JpaRepository<TravelPackage, Long> {
    List<TravelPackage> findByActiveTrue();
}