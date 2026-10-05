package com.bittuthecoder.resultservice.dto;

public record LeaderboardEntryResponse(
        int rank,
        String studentEmail,
        double score,
        double percentage,
        double percentile,
        boolean passed
) {}
