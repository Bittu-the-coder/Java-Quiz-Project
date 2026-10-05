package com.bittuthecoder.quiz_app.repository;

import com.bittuthecoder.quiz_app.models.QuizAttemptModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface QuizAttemptRepository extends JpaRepository<QuizAttemptModel, UUID> {

    Optional<QuizAttemptModel> findByQuizIdAndStudentId(UUID quizId, UUID studentId);
}
