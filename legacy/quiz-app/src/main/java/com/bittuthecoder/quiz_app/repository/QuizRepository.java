package com.bittuthecoder.quiz_app.repository;
import com.bittuthecoder.quiz_app.models.QuizModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuizRepository extends JpaRepository<QuizModel, UUID> {

    List<QuizModel> findByIsActiveTrue();
}

