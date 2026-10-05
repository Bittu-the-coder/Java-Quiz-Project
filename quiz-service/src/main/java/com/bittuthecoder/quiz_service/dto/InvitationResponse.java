package com.bittuthecoder.quiz_service.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record InvitationResponse(
        UUID id,
        UUID quizId,
        String candidateEmail,
        String token,
        String status,
        LocalDateTime invitedAt
) {}
