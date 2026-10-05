package com.bittuthecoder.question_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Candidate-facing question response.
 * Delivers questions and options with zero answer-key leakage.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateQuestionResponse {
    private UUID id;
    private UUID quizId;
    private String text;
    private com.bittuthecoder.question_service.model.Difficulty difficulty;
    private String category;
    private String tags;
    private int marks;
    private List<CandidateOptionResponse> options;
}
