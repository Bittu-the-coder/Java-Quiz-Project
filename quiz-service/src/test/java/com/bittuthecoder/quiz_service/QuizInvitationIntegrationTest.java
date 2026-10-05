package com.bittuthecoder.quiz_service;

import com.bittuthecoder.common.test.BasePostgresIntegrationTest;
import com.bittuthecoder.quiz_service.dto.BulkInviteRequest;
import com.bittuthecoder.quiz_service.dto.BulkInviteResponse;
import com.bittuthecoder.quiz_service.dto.CreateQuizRequest;
import com.bittuthecoder.quiz_service.dto.QuizResponse;
import com.bittuthecoder.quiz_service.repository.QuizInvitationRepository;
import com.bittuthecoder.quiz_service.repository.QuizRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class QuizInvitationIntegrationTest extends BasePostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuizInvitationRepository invitationRepository;

    @AfterEach
    void tearDown() {
        invitationRepository.deleteAll();
        quizRepository.deleteAll();
    }

    @Test
    @DisplayName("Bulk candidate invitations with duplicate handling and token retrieval")
    void testBulkCandidateInvitations() throws Exception {
        UUID orgId = UUID.randomUUID();

        // 1. Create a Quiz
        CreateQuizRequest createQuiz = new CreateQuizRequest();
        createQuiz.setTitle("AWS Certified Cloud Practitioner");
        createQuiz.setDescription("Preliminary Screening");

        String quizResponseStr = mockMvc.perform(post("/api/quizzes")
                        .header("X-Org-Id", orgId.toString())
                        .header("X-User-Role", "ADMIN")
                        .header("X-User-Email", "instructor@institute.edu")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createQuiz)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        QuizResponse createdQuiz = objectMapper.readValue(quizResponseStr, QuizResponse.class);
        UUID quizId = createdQuiz.getId();

        // 2. Bulk invite 3 candidates
        BulkInviteRequest inviteRequest = new BulkInviteRequest(List.of(
                "candidate1@gmail.com",
                "candidate2@gmail.com",
                "candidate3@gmail.com"
        ));

        String inviteRespStr = mockMvc.perform(post("/api/quizzes/" + quizId + "/invitations/bulk")
                        .header("X-Org-Id", orgId.toString())
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inviteRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalRequested").value(3))
                .andExpect(jsonPath("$.successfullyInvited").value(3))
                .andExpect(jsonPath("$.duplicatesSkipped").value(0))
                .andReturn().getResponse().getContentAsString();

        BulkInviteResponse inviteResp = objectMapper.readValue(inviteRespStr, BulkInviteResponse.class);
        assertThat(inviteResp.invitations()).hasSize(3);
        String token = inviteResp.invitations().get(0).token();

        // 3. Re-inviting candidate1 (duplicate) is gracefully skipped
        BulkInviteRequest duplicateRequest = new BulkInviteRequest(List.of(
                "candidate1@gmail.com",
                "candidate4@gmail.com"
        ));

        mockMvc.perform(post("/api/quizzes/" + quizId + "/invitations/bulk")
                        .header("X-Org-Id", orgId.toString())
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalRequested").value(2))
                .andExpect(jsonPath("$.successfullyInvited").value(1))
                .andExpect(jsonPath("$.duplicatesSkipped").value(1));

        // 4. Candidate validates invitation by token
        mockMvc.perform(get("/api/quizzes/invitation/" + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quizId").value(quizId.toString()))
                .andExpect(jsonPath("$.candidateEmail").value("candidate1@gmail.com"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        // 5. Admin queries list of all invitations for quiz
        mockMvc.perform(get("/api/quizzes/" + quizId + "/invitations")
                        .header("X-Org-Id", orgId.toString())
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
    }
}
