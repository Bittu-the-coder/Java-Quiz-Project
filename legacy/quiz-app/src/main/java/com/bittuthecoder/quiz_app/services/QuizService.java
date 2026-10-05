package com.bittuthecoder.quiz_app.services;


import com.bittuthecoder.quiz_app.dtos.CreateQuizRequest;
import com.bittuthecoder.quiz_app.dtos.QuizResponse;

import java.util.List;
import java.util.UUID;

public interface QuizService {

    QuizResponse createQuiz(CreateQuizRequest request);

    QuizResponse activateQuiz(UUID quizId);

    List<QuizResponse> getAllQuizzes();

    QuizResponse getQuizById(UUID quizId);
}
