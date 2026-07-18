package com.travelify.repository;

import com.travelify.model.AgentTrip;
import com.travelify.model.PublishStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AgentTripRepository extends JpaRepository<AgentTrip, Long> {

    List<AgentTrip> findByAgentId(Long agentId);

    List<AgentTrip> findByAgentIdAndStatus(Long agentId, PublishStatus status);

    Page<AgentTrip> findByAgentId(Long agentId, Pageable pageable);

    Page<AgentTrip> findByAgentIdAndStatus(Long agentId, PublishStatus status, Pageable pageable);

    List<AgentTrip> findByStatus(PublishStatus status);

    List<AgentTrip> findByStatusAndIsFeaturedTrue(PublishStatus status);

    List<AgentTrip> findByTemplateId(Long templateId);

    long countByTemplateId(Long templateId);

    boolean existsByTemplateId(Long templateId);

    List<AgentTrip> findByCategoryIdAndStatus(Long categoryId, PublishStatus status);

    Optional<AgentTrip> findByAgentIdAndSlug(Long agentId, String slug);

    boolean existsByAgentIdAndSlug(Long agentId, String slug);

    boolean existsByAgentIdAndSlugAndIdNot(Long agentId, String slug, Long id);

    @Query("""
            SELECT t FROM AgentTrip t
            WHERE (:agentId IS NULL OR t.agent.id = :agentId)
              AND (:status IS NULL OR t.status = :status)
              AND (:categoryId IS NULL OR t.category.id = :categoryId)
              AND (:featured IS NULL OR t.isFeatured = :featured)
              AND (
                    :search IS NULL OR :search = ''
                    OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(t.slug) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """)
    Page<AgentTrip> search(
            @Param("agentId") Long agentId,
            @Param("status") PublishStatus status,
            @Param("categoryId") Long categoryId,
            @Param("featured") Boolean featured,
            @Param("search") String search,
            Pageable pageable
    );
}
