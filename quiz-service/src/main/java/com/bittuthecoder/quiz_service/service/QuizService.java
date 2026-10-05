package com.bittuthecoder.quiz_service.service;

import com.bittuthecoder.quiz_service.dto.QuizResponse;
import com.bittuthecoder.quiz_service.model.Quiz;

import java.util.List;
import java.util.UUID;

public interface QuizService {

    QuizResponse createQuiz(Quiz quiz, UUID orgId, String creatorEmail);

    List<QuizResponse> getAllQuizzes(UUID orgId);

    List<QuizResponse> getPublishedQuizzes(UUID orgId);

    QuizResponse publishQuiz(UUID quizId, UUID orgId);

    QuizResponse getQuizById(UUID quizId, UUID orgId);

    com.bittuthecoder.quiz_service.dto.BulkInviteResponse bulkInviteCandidates(
            UUID quizId, com.bittuthecoder.quiz_service.dto.BulkInviteRequest request, UUID orgId);

    List<com.bittuthecoder.quiz_service.dto.InvitationResponse> getInvitationsByQuiz(UUID quizId, UUID orgId);

    com.bittuthecoder.quiz_service.dto.InvitationResponse getInvitationByToken(String token);
}
