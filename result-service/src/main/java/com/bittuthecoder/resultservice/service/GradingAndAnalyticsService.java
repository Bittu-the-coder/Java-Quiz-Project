package com.bittuthecoder.resultservice.service;

import com.bittuthecoder.resultservice.dto.CohortAnalyticsResponse;
import com.bittuthecoder.resultservice.dto.ItemAnalysisResponse;
import com.bittuthecoder.resultservice.dto.LeaderboardEntryResponse;

import java.util.List;
import java.util.UUID;

public interface GradingAndAnalyticsService {

    void calculateAndPersistExamResults(UUID quizId, UUID orgId);

    CohortAnalyticsResponse getCohortAnalytics(UUID quizId, UUID orgId);

    List<LeaderboardEntryResponse> getLeaderboard(UUID quizId, UUID orgId);

    List<ItemAnalysisResponse> getItemAnalysis(UUID quizId, UUID orgId);
}
