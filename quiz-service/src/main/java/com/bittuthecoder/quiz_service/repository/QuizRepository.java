package com.bittuthecoder.quiz_service.repository;

import com.bittuthecoder.quiz_service.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuizRepository extends JpaRepository<Quiz, UUID> {

    List<Quiz> findByOrgId(UUID orgId);

    List<Quiz> findByOrgIdAndPublishedTrue(UUID orgId);

    Optional<Quiz> findByIdAndOrgId(UUID id, UUID orgId);

    boolean existsByIdAndOrgId(UUID id, UUID orgId);
}
