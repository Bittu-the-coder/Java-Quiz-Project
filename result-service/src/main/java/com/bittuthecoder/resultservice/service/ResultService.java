package com.bittuthecoder.resultservice.service;

import com.bittuthecoder.resultservice.dto.*;

import java.util.List;
import java.util.UUID;

public interface ResultService {

    StartAttemptResponse startAttempt(StartAttemptRequest request, UUID orgId, String studentEmail);

    AutosaveAnswerResponse autosaveAnswer(UUID attemptId, AutosaveAnswerRequest request, UUID orgId, String studentEmail);

    ResumeAttemptResponse resumeAttempt(UUID attemptId, UUID orgId, String studentEmail);

    SubmitAttemptResponse submitAttempt(UUID attemptId, UUID orgId, String studentEmail);

    ResultResponse submitQuiz(
            SubmitQuizRequest request,
            UUID orgId,
            String studentEmail
    );

    List<AttemptSummaryResponse> getStudentAttempts(UUID orgId, String studentEmail);

    List<AttemptSummaryResponse> getQuizAttempts(UUID orgId, UUID quizId);

    AttemptSummaryResponse getAttemptById(UUID id, UUID orgId);
}

