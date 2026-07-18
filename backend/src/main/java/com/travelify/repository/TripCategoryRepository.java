package com.travelify.repository;

import com.travelify.model.TripCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TripCategoryRepository extends JpaRepository<TripCategory, Long> {

    Optional<TripCategory> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<TripCategory> findByIsActiveTrueOrderBySortOrderAsc();

    List<TripCategory> findAllByOrderBySortOrderAsc();
}
