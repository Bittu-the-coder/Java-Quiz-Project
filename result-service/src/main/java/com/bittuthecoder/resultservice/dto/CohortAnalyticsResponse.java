package com.bittuthecoder.resultservice.dto;

import java.util.UUID;

public record CohortAnalyticsResponse(
        UUID quizId,
        int totalSubmissions,
        double averageScore,
        double highestScore,
        double lowestScore,
        double passRatePercentage
) {}
