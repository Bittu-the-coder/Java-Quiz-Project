package com.bittuthecoder.quiz_service.repository;

import com.bittuthecoder.quiz_service.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuizRepository extends JpaRepository<Quiz, UUID> {

    List<Quiz> findByPublishedTrue();
}
