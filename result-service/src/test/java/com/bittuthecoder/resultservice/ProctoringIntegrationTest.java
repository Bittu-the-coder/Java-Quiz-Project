package com.bittuthecoder.resultservice;

import com.bittuthecoder.common.test.BasePostgresIntegrationTest;
import com.bittuthecoder.resultservice.dto.ProctorEventRequest;
import com.bittuthecoder.resultservice.dto.StartAttemptRequest;
import com.bittuthecoder.resultservice.model.AttemptStatus;
import com.bittuthecoder.resultservice.model.ProctorEventType;
import com.bittuthecoder.resultservice.model.QuizAttempt;
import com.bittuthecoder.resultservice.repository.ProctorEventRepository;
import com.bittuthecoder.resultservice.repository.QuizAttemptRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class ProctoringIntegrationTest extends BasePostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private ProctorEventRepository proctorEventRepository;

    @AfterEach
    void tearDown() {
        proctorEventRepository.deleteAll();
        quizAttemptRepository.deleteAll();
    }

    @Test
    @DisplayName("Monotonic proctor event ingestion, replay protection, violation thresholds, and audit retrieval")
    void testProctorEventIngestionAndViolationPolicy() throws Exception {
        UUID orgId = UUID.randomUUID();
        UUID quizId = UUID.randomUUID();
        String studentEmail = "candidate@proctor-test.com";

        // 1. Start an attempt
        StartAttemptRequest startRequest = new StartAttemptRequest(quizId, 60);
        String startResponseStr = mockMvc.perform(post("/api/attempts/start")
                        .header("X-Org-Id", orgId.toString())
                        .header("X-User-Email", studentEmail)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UUID attemptId = UUID.fromString(objectMapper.readTree(startResponseStr).get("attemptId").asText());

        // 2. Ingest sequence 1: FOCUS_LOST
        ProctorEventRequest ev1 = new ProctorEventRequest(
                ProctorEventType.FOCUS_LOST, 1, LocalDateTime.now(), "sig-1", "Switched out of tab");
        mockMvc.perform(post("/api/attempts/" + attemptId + "/proctor-events")
                        .header("X-Org-Id", orgId.toString())
                        .header("X-User-Email", studentEmail)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ev1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sequenceNum").value(1))
                .andExpect(jsonPath("$.actionTaken").value("RECORDED"))
                .andExpect(jsonPath("$.attemptStatus").value("IN_PROGRESS"));

        // 3. Replay attack: resubmitting sequence 1 must be rejected with 400 Bad Request
        mockMvc.perform(post("/api/attempts/" + attemptId + "/proctor-events")
                        .header("X-Org-Id", orgId.toString())
                        .header("X-User-Email", studentEmail)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ev1)))
                .andExpect(status().isBadRequest());

        // 4. Missing event attack: sending sequence 3 instead of 2 must be rejected with 400 Bad Request
        ProctorEventRequest ev3 = new ProctorEventRequest(
                ProctorEventType.TAB_SWITCH, 3, LocalDateTime.now(), "sig-3", "Tab switch");
        mockMvc.perform(post("/api/attempts/" + attemptId + "/proctor-events")
                        .header("X-Org-Id", orgId.toString())
                        .header("X-User-Email", studentEmail)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ev3)))
                .andExpect(status().isBadRequest());

        // 5. Sequential ingestion:
        // Sequence 2: TAB_SWITCH -> RECORDED
        ProctorEventRequest ev2 = new ProctorEventRequest(
                ProctorEventType.TAB_SWITCH, 2, LocalDateTime.now(), "sig-2", "Tab switch");
        mockMvc.perform(post("/api/attempts/" + attemptId + "/proctor-events")
                        .header("X-Org-Id", orgId.toString())
                        .header("X-User-Email", studentEmail)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ev2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.actionTaken").value("RECORDED"));

        // Sequence 3: FULLSCREEN_EXIT -> WARNING threshold reached!
        mockMvc.perform(post("/api/attempts/" + attemptId + "/proctor-events")
                        .header("X-Org-Id", orgId.toString())
                        .header("X-User-Email", studentEmail)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ev3)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.actionTaken").value("WARNING"))
                .andExpect(jsonPath("$.attemptStatus").value("IN_PROGRESS"));

        // Sequence 4: PASTE_DETECTED -> WARNING (3 or 4 violations)
        ProctorEventRequest ev4 = new ProctorEventRequest(
                ProctorEventType.PASTE_DETECTED, 4, LocalDateTime.now(), "sig-4", "Paste");
        mockMvc.perform(post("/api/attempts/" + attemptId + "/proctor-events")
                        .header("X-Org-Id", orgId.toString())
                        .header("X-User-Email", studentEmail)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ev4)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.actionTaken").value("WARNING"));

        // Sequence 5: DEVTOOLS_OPENED -> TERMINATED threshold reached!
        ProctorEventRequest ev5 = new ProctorEventRequest(
                ProctorEventType.DEVTOOLS_OPENED, 5, LocalDateTime.now(), "sig-5", "DevTools");
        mockMvc.perform(post("/api/attempts/" + attemptId + "/proctor-events")
                        .header("X-Org-Id", orgId.toString())
                        .header("X-User-Email", studentEmail)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ev5)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.actionTaken").value("TERMINATED"))
                .andExpect(jsonPath("$.attemptStatus").value("TERMINATED"));

        // Verify attempt is marked TERMINATED in DB
        QuizAttempt dbAttempt = quizAttemptRepository.findById(attemptId).orElseThrow();
        assertThat(dbAttempt.getStatus()).isEqualTo(AttemptStatus.TERMINATED);

        // Sequence 6 on TERMINATED attempt is rejected with 409 Conflict
        ProctorEventRequest ev6 = new ProctorEventRequest(
                ProctorEventType.FOCUS_LOST, 6, LocalDateTime.now(), "sig-6", "Focus");
        mockMvc.perform(post("/api/attempts/" + attemptId + "/proctor-events")
                        .header("X-Org-Id", orgId.toString())
                        .header("X-User-Email", studentEmail)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ev6)))
                .andExpect(status().isConflict());

        // 6. Admin audit trail retrieval
        mockMvc.perform(get("/api/attempts/" + attemptId + "/proctor-events")
                        .header("X-Org-Id", orgId.toString())
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].sequenceNum").value(1))
                .andExpect(jsonPath("$[4].sequenceNum").value(5));

        // Non-admin cannot view proctor audit trail
        mockMvc.perform(get("/api/attempts/" + attemptId + "/proctor-events")
                        .header("X-Org-Id", orgId.toString())
                        .header("X-User-Role", "STUDENT"))
                .andExpect(status().isForbidden());
    }
}
