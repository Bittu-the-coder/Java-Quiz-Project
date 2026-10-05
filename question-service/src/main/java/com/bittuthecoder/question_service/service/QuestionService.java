package com.bittuthecoder.question_service.service;

import com.bittuthecoder.question_service.dto.AdminQuestionResponse;
import com.bittuthecoder.question_service.dto.CandidateQuestionResponse;
import com.bittuthecoder.question_service.model.Question;

import java.util.List;
import java.util.UUID;

public interface QuestionService {

    AdminQuestionResponse createQuestion(Question question, UUID orgId);

    List<CandidateQuestionResponse> getCandidateQuestionsByQuiz(UUID quizId, UUID orgId);

    List<AdminQuestionResponse> getAdminQuestionsByQuiz(UUID quizId, UUID orgId);

    CandidateQuestionResponse getCandidateQuestionById(UUID questionId, UUID orgId);

    AdminQuestionResponse getAdminQuestionById(UUID questionId, UUID orgId);
}
