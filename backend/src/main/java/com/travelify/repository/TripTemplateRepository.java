package com.travelify.repository;

import com.travelify.model.PublishStatus;
import com.travelify.model.TripTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TripTemplateRepository extends JpaRepository<TripTemplate, Long> {

    Optional<TripTemplate> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    List<TripTemplate> findByStatus(PublishStatus status);

    List<TripTemplate> findByStatusAndIsFeaturedTrue(PublishStatus status);

    List<TripTemplate> findByCategoryId(Long categoryId);

    List<TripTemplate> findByCategoryIdAndStatus(Long categoryId, PublishStatus status);

    List<TripTemplate> findByCreatedById(Long createdById);

    List<TripTemplate> findByCreatedByIdAndStatus(Long createdById, PublishStatus status);

    Page<TripTemplate> findByStatus(PublishStatus status, Pageable pageable);

    @Query("""
            SELECT t FROM TripTemplate t
            WHERE (:status IS NULL OR t.status = :status)
              AND (:categoryId IS NULL OR t.category.id = :categoryId)
              AND (:featured IS NULL OR t.isFeatured = :featured)
              AND (
                    :search IS NULL OR :search = ''
                    OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(t.slug) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """)
    Page<TripTemplate> search(
            @Param("status") PublishStatus status,
            @Param("categoryId") Long categoryId,
            @Param("featured") Boolean featured,
            @Param("search") String search,
            Pageable pageable
    );
}
