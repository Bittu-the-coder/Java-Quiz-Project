package com.bittuthecoder.question_service.service;

import com.bittuthecoder.question_service.model.Question;

import java.util.List;
import java.util.UUID;

public interface QuestionService {

    Question createQuestion(Question question);

    List<Question> getQuestionsByQuiz(UUID quizId);
}
