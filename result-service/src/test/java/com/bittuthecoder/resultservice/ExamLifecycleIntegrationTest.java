package com.bittuthecoder.resultservice;

import com.bittuthecoder.common.test.BasePostgresIntegrationTest;
import com.bittuthecoder.resultservice.dto.AutosaveAnswerRequest;
import com.bittuthecoder.resultservice.dto.StartAttemptRequest;
import com.bittuthecoder.resultservice.feign.QuestionClient;
import com.bittuthecoder.resultservice.model.Answer;
import com.bittuthecoder.resultservice.model.AttemptStatus;
import com.bittuthecoder.resultservice.model.QuizAttempt;
import com.bittuthecoder.resultservice.repository.AnswerRepository;
import com.bittuthecoder.resultservice.repository.QuizAttemptRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class ExamLifecycleIntegrationTest extends BasePostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @MockitoBean
    private QuestionClient questionClient;

    @AfterEach
    void tearDown() {
        answerRepository.deleteAll();
        quizAttemptRepository.deleteAll();
    }

    @Test
    @DisplayName("Full Exam Lifecycle: Start -> Autosave (with Upsert) -> Resume -> Submit (Idempotent)")
    void testFullExamLifecycle() throws Exception {
        UUID orgId = UUID.randomUUID();
        UUID quizId = UUID.randomUUID();
        UUID question1 = UUID.randomUUID();
        UUID question2 = UUID.randomUUID();
        UUID optionA = UUID.randomUUID();
        UUID optionB = UUID.randomUUID();
        String studentEmail = "candidate@test.com";

        // 1. Start Attempt
        StartAttemptRequest startRequest = new StartAttemptRequest(quizId, 45);
        String startResponseStr = mockMvc.perform(post("/api/attempts/start")
                        .header("X-Org-Id", orgId.toString())
                        .header("X-User-Email", studentEmail)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.attemptId").isNotEmpty())
                .andExpect(jsonPath("$.quizId").value(quizId.toString()))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.remainingSeconds").isNumber())
                .andReturn().getResponse().getContentAsString();

        UUID attemptId = UUID.fromString(objectMapper.readTree(startResponseStr).get("attemptId").asText());

        // Idempotent start returns same attemptId
        mockMvc.perform(post("/api/attempts/start")
                        .header("X-Org-Id", orgId.toString())
                        .header("X-User-Email", studentEmail)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.attemptId").value(attemptId.toString()));

        // 2. Autosave Answer 1
        AutosaveAnswerRequest saveQ1 = new AutosaveAnswerRequest(question1, optionA);
        mockMvc.perform(put("/api/attempts/" + attemptId + "/answer")
                        .header("X-Org-Id", orgId.toString())
                        .header("X-User-Email", studentEmail)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveQ1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questionId").value(question1.toString()))
                .andExpect(jsonPath("$.selectedOptionId").value(optionA.toString()))
                .andExpect(jsonPath("$.savedAt").isNotEmpty());

        // Upsert Answer 1 with changed option
        AutosaveAnswerRequest updateQ1 = new AutosaveAnswerRequest(question1, optionB);
        mockMvc.perform(put("/api/attempts/" + attemptId + "/answer")
                        .header("X-Org-Id", orgId.toString())
                        .header("X-User-Email", studentEmail)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateQ1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.selectedOptionId").value(optionB.toString()));

        // Autosave Answer 2
        AutosaveAnswerRequest saveQ2 = new AutosaveAnswerRequest(question2, optionA);
        mockMvc.perform(put("/api/attempts/" + attemptId + "/answer")
                        .header("X-Org-Id", orgId.toString())
                        .header("X-User-Email", studentEmail)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveQ2)))
                .andExpect(status().isOk());

        // Verify DB only has 2 answer rows for this attempt
        List<Answer> dbAnswers = answerRepository.findByAttemptId(attemptId);
        assertThat(dbAnswers).hasSize(2);
        Answer savedQ1 = dbAnswers.stream().filter(a -> a.getQuestionId().equals(question1)).findFirst().orElseThrow();
        assertThat(savedQ1.getSelectedOptionId()).isEqualTo(optionB);

        // 3. Resume Attempt
        mockMvc.perform(get("/api/attempts/" + attemptId + "/resume")
                        .header("X-Org-Id", orgId.toString())
                        .header("X-User-Email", studentEmail))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attemptId").value(attemptId.toString()))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.savedAnswers['" + question1 + "']").value(optionB.toString()))
                .andExpect(jsonPath("$.savedAnswers['" + question2 + "']").value(optionA.toString()));

        // 4. Submit Attempt
        when(questionClient.validateAnswers(anyMap())).thenReturn(Map.of(
                question1, true,
                question2, false
        ));

        mockMvc.perform(post("/api/attempts/" + attemptId + "/submit")
                        .header("X-Org-Id", orgId.toString())
                        .header("X-User-Email", studentEmail))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attemptId").value(attemptId.toString()))
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.score").value(1))
                .andExpect(jsonPath("$.submittedAt").isNotEmpty());

        // 5. Idempotent Submit
        mockMvc.perform(post("/api/attempts/" + attemptId + "/submit")
                        .header("X-Org-Id", orgId.toString())
                        .header("X-User-Email", studentEmail))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.score").value(1));

        // 6. Autosave after submission must fail with 409 Conflict
        mockMvc.perform(put("/api/attempts/" + attemptId + "/answer")
                        .header("X-Org-Id", orgId.toString())
                        .header("X-User-Email", studentEmail)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveQ1)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Server Deadline Expiry: Autosave after deadline triggers Auto-Submission and returns 410 Gone")
    void testServerDeadlineExpiry() throws Exception {
        UUID orgId = UUID.randomUUID();
        UUID quizId = UUID.randomUUID();
        UUID questionId = UUID.randomUUID();
        UUID optionId = UUID.randomUUID();
        String studentEmail = "late_student@test.com";

        // Seed an attempt whose server deadline has passed
        QuizAttempt expiredAttempt = QuizAttempt.builder()
                .orgId(orgId)
                .quizId(quizId)
                .studentEmail(studentEmail)
                .status(AttemptStatus.IN_PROGRESS)
                .startedAt(LocalDateTime.now().minusMinutes(70))
                .serverDeadline(LocalDateTime.now().minusMinutes(10))
                .durationMinutes(60)
                .score(0)
                .build();
        expiredAttempt = quizAttemptRepository.save(expiredAttempt);

        // Autosave attempted after deadline
        AutosaveAnswerRequest request = new AutosaveAnswerRequest(questionId, optionId);
        mockMvc.perform(put("/api/attempts/" + expiredAttempt.getId() + "/answer")
                        .header("X-Org-Id", orgId.toString())
                        .header("X-User-Email", studentEmail)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isGone());

        // Verify status transitioned to AUTO_SUBMITTED in database
        QuizAttempt updated = quizAttemptRepository.findById(expiredAttempt.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(AttemptStatus.AUTO_SUBMITTED);
        assertThat(updated.getSubmittedAt()).isNotNull();
    }
}
