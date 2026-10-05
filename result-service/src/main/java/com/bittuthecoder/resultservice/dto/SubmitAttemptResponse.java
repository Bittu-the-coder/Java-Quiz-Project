package com.bittuthecoder.resultservice.dto;

import com.bittuthecoder.resultservice.model.AttemptStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record SubmitAttemptResponse(
        UUID attemptId,
        UUID quizId,
        AttemptStatus status,
        int score,
        LocalDateTime submittedAt
) {}
