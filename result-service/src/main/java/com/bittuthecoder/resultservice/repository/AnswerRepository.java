package com.bittuthecoder.resultservice.repository;

import com.bittuthecoder.resultservice.model.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AnswerRepository
        extends JpaRepository<QuizAttempt, UUID> {
}
