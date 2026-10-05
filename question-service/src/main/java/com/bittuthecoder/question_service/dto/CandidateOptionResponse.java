package com.bittuthecoder.question_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Candidate-facing option response.
 * CRITICAL SECURITY: 'correct' field is omitted to prevent answer-key leaks to candidates.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateOptionResponse {
    private UUID id;
    private String text;
}
