package com.bittuthecoder.resultservice.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AutosaveAnswerRequest(
        @NotNull(message = "questionId is required") UUID questionId,
        UUID selectedOptionId
) {}
