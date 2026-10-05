package com.bittuthecoder.quiz_app.services;

import com.bittuthecoder.quiz_app.dtos.CreateOptionRequest;
import com.bittuthecoder.quiz_app.dtos.CreateQuestionRequest;
import com.bittuthecoder.quiz_app.dtos.QuestionResponse;

import java.util.List;
import java.util.UUID;

public interface QuestionService {

    void addQuestion(CreateQuestionRequest request);

    void addOption(CreateOptionRequest request);

    List<QuestionResponse> getQuestionsByQuiz(UUID quizId);
}

