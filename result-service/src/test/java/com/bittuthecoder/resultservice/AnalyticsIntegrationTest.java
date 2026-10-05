package com.bittuthecoder.resultservice;

import com.bittuthecoder.common.test.BasePostgresIntegrationTest;
import com.bittuthecoder.resultservice.dto.AutosaveAnswerRequest;
import com.bittuthecoder.resultservice.dto.StartAttemptRequest;
import com.bittuthecoder.resultservice.model.Answer;
import com.bittuthecoder.resultservice.model.AttemptStatus;
import com.bittuthecoder.resultservice.model.QuizAttempt;
import com.bittuthecoder.resultservice.repository.AnswerRepository;
import com.bittuthecoder.resultservice.repository.ExamResultRepository;
import com.bittuthecoder.resultservice.repository.ItemAnalysisRepository;
import com.bittuthecoder.resultservice.repository.QuizAttemptRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class AnalyticsIntegrationTest extends BasePostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private ExamResultRepository examResultRepository;

    @Autowired
    private ItemAnalysisRepository itemAnalysisRepository;

    @AfterEach
    void tearDown() {
        itemAnalysisRepository.deleteAll();
        examResultRepository.deleteAll();
        answerRepository.deleteAll();
        quizAttemptRepository.deleteAll();
    }

    @Test
    @DisplayName("Cohort grading, leaderboard rankings, psychometric item analysis, and tenant isolation")
    void testCohortGradingAndPsychometricAnalytics() throws Exception {
        UUID orgId = UUID.randomUUID();
        UUID otherOrgId = UUID.randomUUID();
        UUID quizId = UUID.randomUUID();

        UUID q1 = UUID.randomUUID();
        UUID q2 = UUID.randomUUID();
        UUID q3 = UUID.randomUUID();

        // 1. Candidate A (Top performer: 3/3)
        QuizAttempt attemptA = quizAttemptRepository.save(QuizAttempt.builder()
                .orgId(orgId)
                .quizId(quizId)
                .studentEmail("alice@test.com")
                .status(AttemptStatus.SUBMITTED)
                .score(3)
                .durationMinutes(60)
                .startedAt(LocalDateTime.now().minusMinutes(40))
                .submittedAt(LocalDateTime.now().minusMinutes(10))
                .serverDeadline(LocalDateTime.now().plusMinutes(20))
                .build());

        answerRepository.save(Answer.builder().attempt(attemptA).questionId(q1).selectedOptionId(UUID.randomUUID()).correct(true).build());
        answerRepository.save(Answer.builder().attempt(attemptA).questionId(q2).selectedOptionId(UUID.randomUUID()).correct(true).build());
        answerRepository.save(Answer.builder().attempt(attemptA).questionId(q3).selectedOptionId(UUID.randomUUID()).correct(true).build());

        // 2. Candidate B (Mid performer: 2/3)
        QuizAttempt attemptB = quizAttemptRepository.save(QuizAttempt.builder()
                .orgId(orgId)
                .quizId(quizId)
                .studentEmail("bob@test.com")
                .status(AttemptStatus.SUBMITTED)
                .score(2)
                .durationMinutes(60)
                .startedAt(LocalDateTime.now().minusMinutes(50))
                .submittedAt(LocalDateTime.now().minusMinutes(5))
                .serverDeadline(LocalDateTime.now().plusMinutes(10))
                .build());

        answerRepository.save(Answer.builder().attempt(attemptB).questionId(q1).selectedOptionId(UUID.randomUUID()).correct(true).build());
        answerRepository.save(Answer.builder().attempt(attemptB).questionId(q2).selectedOptionId(UUID.randomUUID()).correct(true).build());
        answerRepository.save(Answer.builder().attempt(attemptB).questionId(q3).selectedOptionId(UUID.randomUUID()).correct(false).build());

        // 3. Candidate C (Low performer: 0/3)
        QuizAttempt attemptC = quizAttemptRepository.save(QuizAttempt.builder()
                .orgId(orgId)
                .quizId(quizId)
                .studentEmail("charlie@test.com")
                .status(AttemptStatus.AUTO_SUBMITTED)
                .score(0)
                .durationMinutes(60)
                .startedAt(LocalDateTime.now().minusMinutes(65))
                .submittedAt(LocalDateTime.now().minusMinutes(5))
                .serverDeadline(LocalDateTime.now().minusMinutes(5))
                .build());

        answerRepository.save(Answer.builder().attempt(attemptC).questionId(q1).selectedOptionId(UUID.randomUUID()).correct(false).build());
        answerRepository.save(Answer.builder().attempt(attemptC).questionId(q2).selectedOptionId(UUID.randomUUID()).correct(false).build());
        answerRepository.save(Answer.builder().attempt(attemptC).questionId(q3).selectedOptionId(UUID.randomUUID()).correct(false).build());

        // --- Test 1: Leaderboard Verification ---
        mockMvc.perform(get("/api/results/quiz/" + quizId + "/leaderboard")
                        .header("X-Org-Id", orgId.toString())
                        .header("X-User-Email", "alice@test.com")
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].studentEmail").value("alice@test.com"))
                .andExpect(jsonPath("$[0].rank").value(1))
                .andExpect(jsonPath("$[0].score").value(3.0))
                .andExpect(jsonPath("$[0].percentile").value(100.0))
                .andExpect(jsonPath("$[0].passed").value(true))
                .andExpect(jsonPath("$[1].studentEmail").value("bob@test.com"))
                .andExpect(jsonPath("$[1].rank").value(2))
                .andExpect(jsonPath("$[1].score").value(2.0))
                .andExpect(jsonPath("$[1].percentile").value(50.0))
                .andExpect(jsonPath("$[1].passed").value(true))
                .andExpect(jsonPath("$[2].studentEmail").value("charlie@test.com"))
                .andExpect(jsonPath("$[2].rank").value(3))
                .andExpect(jsonPath("$[2].score").value(0.0))
                .andExpect(jsonPath("$[2].percentile").value(0.0))
                .andExpect(jsonPath("$[2].passed").value(false));

        // --- Test 2: Cohort Analytics (Admin Only) ---
        // Verify non-admin gets 403
        mockMvc.perform(get("/api/results/quiz/" + quizId + "/analytics")
                        .header("X-Org-Id", orgId.toString())
                        .header("X-User-Email", "alice@test.com")
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isForbidden());

        // Verify admin gets cohort metrics
        mockMvc.perform(get("/api/results/quiz/" + quizId + "/analytics")
                        .header("X-Org-Id", orgId.toString())
                        .header("X-User-Email", "admin@org.com")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quizId").value(quizId.toString()))
                .andExpect(jsonPath("$.totalSubmissions").value(3))
                .andExpect(jsonPath("$.averageScore").value(1.67))
                .andExpect(jsonPath("$.highestScore").value(3.0))
                .andExpect(jsonPath("$.lowestScore").value(0.0))
                .andExpect(jsonPath("$.passRatePercentage").value(66.67));

        // --- Test 3: Psychometric Item Analysis (Admin Only) ---
        // Verify admin gets item difficulty and discrimination
        mockMvc.perform(get("/api/results/quiz/" + quizId + "/item-analysis")
                        .header("X-Org-Id", orgId.toString())
                        .header("X-User-Email", "admin@org.com")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));

        // --- Test 4: Multi-tenant Isolation ---
        // Other organization cannot access Org 1's leaderboard or analytics
        mockMvc.perform(get("/api/results/quiz/" + quizId + "/leaderboard")
                        .header("X-Org-Id", otherOrgId.toString())
                        .header("X-User-Email", "intruder@other.com")
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(get("/api/results/quiz/" + quizId + "/analytics")
                        .header("X-Org-Id", otherOrgId.toString())
                        .header("X-User-Email", "admin@other.com")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSubmissions").value(0));
    }
}
