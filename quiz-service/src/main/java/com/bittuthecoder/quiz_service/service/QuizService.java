package com.bittuthecoder.quiz_service.service;


import com.bittuthecoder.quiz_service.model.Quiz;

import java.util.List;
import java.util.UUID;

public interface QuizService {

    Quiz createQuiz(Quiz quiz, String creatorEmail);

    List<Quiz> getAllQuizzes();

    List<Quiz> getPublishedQuizzes();

    Quiz publishQuiz(UUID quizId);
}
