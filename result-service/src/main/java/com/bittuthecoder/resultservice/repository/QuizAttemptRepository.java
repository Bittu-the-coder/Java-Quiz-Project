package com.bittuthecoder.resultservice.repository;

import com.bittuthecoder.resultservice.model.AttemptStatus;
import com.bittuthecoder.resultservice.model.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, UUID> {

    List<QuizAttempt> findByOrgIdAndStudentEmail(UUID orgId, String studentEmail);

    List<QuizAttempt> findByOrgIdAndQuizId(UUID orgId, UUID quizId);

    Optional<QuizAttempt> findByIdAndOrgId(UUID id, UUID orgId);

    Optional<QuizAttempt> findByIdAndOrgIdAndStudentEmail(UUID id, UUID orgId, String studentEmail);

    Optional<QuizAttempt> findFirstByOrgIdAndQuizIdAndStudentEmailAndStatusOrderByStartedAtDesc(
            UUID orgId, UUID quizId, String studentEmail, AttemptStatus status);

    List<QuizAttempt> findByStatusAndServerDeadlineBefore(AttemptStatus status, LocalDateTime deadline);

    List<QuizAttempt> findByOrgId(UUID orgId);
}

