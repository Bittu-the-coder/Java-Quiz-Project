package com.bittuthecoder.quiz_app.repository;

import com.bittuthecoder.quiz_app.models.QuizAttemptAnswerModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface QuizAttemptAnswerRepository extends JpaRepository<QuizAttemptAnswerModel, UUID> {
}

