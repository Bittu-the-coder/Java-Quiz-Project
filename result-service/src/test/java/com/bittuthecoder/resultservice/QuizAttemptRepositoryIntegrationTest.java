package com.bittuthecoder.resultservice;

import com.bittuthecoder.common.test.BasePostgresIntegrationTest;
import com.bittuthecoder.resultservice.model.Answer;
import com.bittuthecoder.resultservice.model.QuizAttempt;
import com.bittuthecoder.resultservice.repository.QuizAttemptRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class QuizAttemptRepositoryIntegrationTest extends BasePostgresIntegrationTest {

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @AfterEach
    void tearDown() {
        quizAttemptRepository.deleteAll();
    }

    @Test
    @DisplayName("Should persist quiz attempt with answers and retrieve by orgId and studentEmail")
    void shouldPersistQuizAttemptWithAnswers() {
        UUID orgId = UUID.randomUUID();
        UUID quizId = UUID.randomUUID();
        UUID questionId = UUID.randomUUID();
        UUID selectedOptionId = UUID.randomUUID();

        QuizAttempt attempt = QuizAttempt.builder()
                .orgId(orgId)
                .quizId(quizId)
                .studentEmail("student@assessify.io")
                .score(10)
                .submittedAt(LocalDateTime.now())
                .build();

        Answer answer = Answer.builder()
                .questionId(questionId)
                .selectedOptionId(selectedOptionId)
                .correct(true)
                .attempt(attempt)
                .build();

        attempt.setAnswers(List.of(answer));

        QuizAttempt saved = quizAttemptRepository.save(attempt);
        assertThat(saved.getId()).isNotNull();

        List<QuizAttempt> found = quizAttemptRepository.findByOrgIdAndStudentEmail(orgId, "student@assessify.io");
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getStudentEmail()).isEqualTo("student@assessify.io");
        assertThat(found.get(0).getScore()).isEqualTo(10);
        assertThat(found.get(0).getAnswers()).hasSize(1);
        assertThat(found.get(0).getAnswers().get(0).isCorrect()).isTrue();

        Optional<QuizAttempt> foundById = quizAttemptRepository.findByIdAndOrgId(saved.getId(), orgId);
        assertThat(foundById).isPresent();
    }

    @Test
    @DisplayName("Cross-tenant isolation: Tenant A cannot see quiz attempts from Tenant B")
    void crossTenantIsolationTest() {
        UUID orgA = UUID.randomUUID();
        UUID orgB = UUID.randomUUID();
        UUID quizId = UUID.randomUUID();

        QuizAttempt attemptA = QuizAttempt.builder()
                .orgId(orgA)
                .quizId(quizId)
                .studentEmail("student@shared-domain.com")
                .score(85)
                .submittedAt(LocalDateTime.now())
                .build();

        QuizAttempt attemptB = QuizAttempt.builder()
                .orgId(orgB)
                .quizId(quizId)
                .studentEmail("student@shared-domain.com")
                .score(92)
                .submittedAt(LocalDateTime.now())
                .build();

        quizAttemptRepository.save(attemptA);
        quizAttemptRepository.save(attemptB);

        // Org A queries by student email -> only Org A's attempt returned
        List<QuizAttempt> orgAAttempts = quizAttemptRepository.findByOrgIdAndStudentEmail(orgA, "student@shared-domain.com");
        assertThat(orgAAttempts).hasSize(1);
        assertThat(orgAAttempts.get(0).getScore()).isEqualTo(85);

        // Org A queries attempt B by id -> empty
        Optional<QuizAttempt> crossTenantAttempt = quizAttemptRepository.findByIdAndOrgId(attemptB.getId(), orgA);
        assertThat(crossTenantAttempt).isEmpty();

        // Org B queries by quiz id -> only Org B's attempt returned
        List<QuizAttempt> orgBAttempts = quizAttemptRepository.findByOrgIdAndQuizId(orgB, quizId);
        assertThat(orgBAttempts).hasSize(1);
        assertThat(orgBAttempts.get(0).getScore()).isEqualTo(92);
    }
}
