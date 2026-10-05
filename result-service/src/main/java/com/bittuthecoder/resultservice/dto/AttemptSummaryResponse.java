package com.bittuthecoder.resultservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttemptSummaryResponse {

    private UUID id;
    private UUID orgId;
    private UUID quizId;
    private String studentEmail;
    private com.bittuthecoder.resultservice.model.AttemptStatus status;
    private int score;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
    private LocalDateTime serverDeadline;
    private int totalAnswers;
}
