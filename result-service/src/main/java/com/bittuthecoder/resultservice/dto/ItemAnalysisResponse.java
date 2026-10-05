package com.bittuthecoder.resultservice.dto;

import java.util.UUID;

public record ItemAnalysisResponse(
        UUID questionId,
        int totalAttempts,
        int correctAttempts,
        double difficultyIndex,
        double discriminationIndex,
        String difficultyRating
) {}
