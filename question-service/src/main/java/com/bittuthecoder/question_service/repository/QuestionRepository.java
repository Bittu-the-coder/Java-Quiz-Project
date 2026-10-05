package com.bittuthecoder.question_service.repository;

import com.bittuthecoder.question_service.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuestionRepository extends JpaRepository<Question, UUID> {

    List<Question> findByOrgIdAndQuizId(UUID orgId, UUID quizId);

    Optional<Question> findByIdAndOrgId(UUID id, UUID orgId);

    List<Question> findByOrgId(UUID orgId);

    boolean existsByIdAndOrgId(UUID id, UUID orgId);
}
