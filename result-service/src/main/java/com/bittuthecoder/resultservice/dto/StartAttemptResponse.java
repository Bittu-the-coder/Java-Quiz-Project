package com.bittuthecoder.resultservice.dto;

import com.bittuthecoder.resultservice.model.AttemptStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record StartAttemptResponse(
        UUID attemptId,
        UUID quizId,
        AttemptStatus status,
        LocalDateTime startedAt,
        LocalDateTime serverDeadline,
        long remainingSeconds
) {}
