package com.bittuthecoder.resultservice.repository;

import com.bittuthecoder.resultservice.model.ExamResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExamResultRepository extends JpaRepository<ExamResult, UUID> {

    List<ExamResult> findByOrgIdAndQuizIdOrderByTotalScoreDesc(UUID orgId, UUID quizId);

    Optional<ExamResult> findByAttemptId(UUID attemptId);

    Optional<ExamResult> findByOrgIdAndQuizIdAndStudentEmail(UUID orgId, UUID quizId, String studentEmail);
}
