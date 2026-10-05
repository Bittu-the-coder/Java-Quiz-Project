package com.bittuthecoder.resultservice.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AutosaveAnswerResponse(
        UUID questionId,
        UUID selectedOptionId,
        LocalDateTime savedAt,
        long remainingSeconds
) {}
