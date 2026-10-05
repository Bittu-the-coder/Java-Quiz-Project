package com.bittuthecoder.resultservice.repository;

import com.bittuthecoder.resultservice.model.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AnswerRepository extends JpaRepository<Answer, UUID> {
    Optional<Answer> findByAttemptIdAndQuestionId(UUID attemptId, UUID questionId);
    List<Answer> findByAttemptId(UUID attemptId);
}
