package com.bittuthecoder.question_service.service;

import com.bittuthecoder.question_service.dto.CandidateOptionResponse;
import com.bittuthecoder.question_service.dto.CandidateQuestionResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeterministicPaperGeneratorTest {

    private final DeterministicPaperGenerator generator = new DeterministicPaperGenerator();

    private List<CandidateQuestionResponse> createSampleQuestions(UUID quizId) {
        List<CandidateQuestionResponse> list = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            UUID qId = UUID.randomUUID();
            List<CandidateOptionResponse> options = List.of(
                    new CandidateOptionResponse(UUID.randomUUID(), "Option A for Q" + i),
                    new CandidateOptionResponse(UUID.randomUUID(), "Option B for Q" + i),
                    new CandidateOptionResponse(UUID.randomUUID(), "Option C for Q" + i),
                    new CandidateOptionResponse(UUID.randomUUID(), "Option D for Q" + i)
            );
            list.add(CandidateQuestionResponse.builder()
                    .id(qId)
                    .quizId(quizId)
                    .text("Question " + i)
                    .options(options)
                    .build());
        }
        return list;
    }

    @Test
    @DisplayName("Determinism: Exact same attemptId produces identical question & option order across multiple runs")
    void testDeterminismAcrossInvocations() {
        UUID quizId = UUID.randomUUID();
        UUID attemptId = UUID.randomUUID();
        List<CandidateQuestionResponse> original = createSampleQuestions(quizId);

        List<CandidateQuestionResponse> run1 = generator.generatePaper(original, attemptId, quizId);
        List<CandidateQuestionResponse> run2 = generator.generatePaper(original, attemptId, quizId);
        List<CandidateQuestionResponse> run3 = generator.generatePaper(original, attemptId, quizId);

        // Questions match in exact sequence
        List<UUID> qIdsRun1 = run1.stream().map(CandidateQuestionResponse::getId).toList();
        List<UUID> qIdsRun2 = run2.stream().map(CandidateQuestionResponse::getId).toList();
        List<UUID> qIdsRun3 = run3.stream().map(CandidateQuestionResponse::getId).toList();

        assertThat(qIdsRun1).isEqualTo(qIdsRun2);
        assertThat(qIdsRun2).isEqualTo(qIdsRun3);

        // Options within every question match in exact sequence
        for (int i = 0; i < run1.size(); i++) {
            List<UUID> optIds1 = run1.get(i).getOptions().stream().map(CandidateOptionResponse::getId).toList();
            List<UUID> optIds2 = run2.get(i).getOptions().stream().map(CandidateOptionResponse::getId).toList();
            assertThat(optIds1).isEqualTo(optIds2);
        }
    }

    @Test
    @DisplayName("Anti-Cheating Randomization: Different attemptIds produce different permutations")
    void testDifferentAttemptIdsProduceDifferentOrders() {
        UUID quizId = UUID.randomUUID();
        UUID attemptId1 = UUID.randomUUID();
        UUID attemptId2 = UUID.randomUUID();
        List<CandidateQuestionResponse> original = createSampleQuestions(quizId);

        List<CandidateQuestionResponse> candidate1Paper = generator.generatePaper(original, attemptId1, quizId);
        List<CandidateQuestionResponse> candidate2Paper = generator.generatePaper(original, attemptId2, quizId);

        List<UUID> qIds1 = candidate1Paper.stream().map(CandidateQuestionResponse::getId).toList();
        List<UUID> qIds2 = candidate2Paper.stream().map(CandidateQuestionResponse::getId).toList();

        assertThat(qIds1).isNotEqualTo(qIds2);
    }

    @Test
    @DisplayName("Input Immutability: original list order is preserved")
    void testInputNotMutated() {
        UUID quizId = UUID.randomUUID();
        UUID attemptId = UUID.randomUUID();
        List<CandidateQuestionResponse> original = createSampleQuestions(quizId);
        List<UUID> originalQIds = original.stream().map(CandidateQuestionResponse::getId).toList();

        generator.generatePaper(original, attemptId, quizId);

        List<UUID> afterCallQIds = original.stream().map(CandidateQuestionResponse::getId).toList();
        assertThat(afterCallQIds).isEqualTo(originalQIds);
    }
}
