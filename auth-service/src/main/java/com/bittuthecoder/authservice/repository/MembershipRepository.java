package com.bittuthecoder.authservice.repository;

import com.bittuthecoder.authservice.models.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, UUID> {
    Optional<Membership> findByOrgIdAndUserId(UUID orgId, UUID userId);
    List<Membership> findByUserId(UUID userId);
    List<Membership> findByOrgId(UUID orgId);
    boolean existsByOrgIdAndUserId(UUID orgId, UUID userId);
}
