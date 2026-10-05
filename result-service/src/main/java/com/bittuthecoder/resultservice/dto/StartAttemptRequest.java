package com.bittuthecoder.resultservice.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record StartAttemptRequest(
        @NotNull(message = "quizId is required") UUID quizId,
        Integer durationMinutes
) {}
