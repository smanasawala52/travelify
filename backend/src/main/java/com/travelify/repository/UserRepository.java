package com.travelify.repository;

import com.travelify.model.ProviderType;
import com.travelify.model.Role;
import com.travelify.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<User> findByRole(Role role);

    List<User> findByIsActiveTrue();

    List<User> findByRoleAndIsActiveTrue(Role role);

    List<User> findByProviderType(ProviderType providerType);

    List<User> findByProviderTypeAndIsActiveTrue(ProviderType providerType);

    List<User> findByRoleAndProviderType(Role role, ProviderType providerType);

    long countByRole(Role role);

    long countByRoleAndIsActiveTrue(Role role);

    @Query("""
            SELECT u FROM User u
            WHERE (:role IS NULL OR u.role = :role)
              AND (:isActive IS NULL OR u.isActive = :isActive)
              AND (
                    :search IS NULL OR :search = ''
                    OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """)
    Page<User> searchUsers(@Param("role") Role role,
                           @Param("isActive") Boolean isActive,
                           @Param("search") String search,
                           Pageable pageable);

    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :at, u.updatedAt = :at WHERE u.id = :id")
    int updateLastLoginAt(@Param("id") Long id, @Param("at") Instant at);
}
