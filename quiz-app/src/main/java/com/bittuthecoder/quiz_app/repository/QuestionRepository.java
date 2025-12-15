package com.bittuthecoder.quiz_app.repository;


import com.bittuthecoder.quiz_app.models.QuestionModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuestionRepository extends JpaRepository<QuestionModel, UUID> {

    List<QuestionModel> findByQuizId(UUID quizId);
}
