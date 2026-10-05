package com.bittuthecoder.question_service.service;

import com.bittuthecoder.question_service.dto.CandidateOptionResponse;
import com.bittuthecoder.question_service.dto.CandidateQuestionResponse;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Generates candidate-specific, deterministically shuffled question papers.
 * Ensures:
 * 1. Side-by-side candidates receive different question and option permutations (anti-cheating).
 * 2. Reconnecting candidates with the same attemptId receive the EXACT same permutation every time.
 */
@Component
public class DeterministicPaperGenerator {

    public List<CandidateQuestionResponse> generatePaper(
            List<CandidateQuestionResponse> questions,
            UUID attemptId,
            UUID quizId
    ) {
        if (questions == null || questions.isEmpty()) {
            return Collections.emptyList();
        }

        long seed = computeSeed(attemptId, quizId);

        // Shuffle questions deterministically
        List<CandidateQuestionResponse> shuffledQuestions = new ArrayList<>(questions);
        Collections.shuffle(shuffledQuestions, new Random(seed));

        // Shuffle options per question deterministically
        return shuffledQuestions.stream().map(q -> {
            if (q.getOptions() == null || q.getOptions().isEmpty()) {
                return q;
            }

            long optionSeed = seed ^ (q.getId() != null ? q.getId().getMostSignificantBits() : 0L);
            List<CandidateOptionResponse> shuffledOptions = new ArrayList<>(q.getOptions());
            Collections.shuffle(shuffledOptions, new Random(optionSeed));

            return CandidateQuestionResponse.builder()
                    .id(q.getId())
                    .quizId(q.getQuizId())
                    .text(q.getText())
                    .difficulty(q.getDifficulty())
                    .category(q.getCategory())
                    .tags(q.getTags())
                    .marks(q.getMarks())
                    .options(shuffledOptions)
                    .build();
        }).toList();
    }

    private long computeSeed(UUID attemptId, UUID quizId) {
        try {
            String input = (attemptId != null ? attemptId.toString() : "") + ":" + (quizId != null ? quizId.toString() : "");
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return ByteBuffer.wrap(hash).getLong();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
