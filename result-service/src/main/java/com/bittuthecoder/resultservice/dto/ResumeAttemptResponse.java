package com.bittuthecoder.resultservice.dto;

import com.bittuthecoder.resultservice.model.AttemptStatus;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record ResumeAttemptResponse(
        UUID attemptId,
        UUID quizId,
        AttemptStatus status,
        long remainingSeconds,
        LocalDateTime serverDeadline,
        Map<UUID, UUID> savedAnswers
) {}
