package com.bittuthecoder.quiz_service;

import com.bittuthecoder.common.test.BasePostgresIntegrationTest;
import com.bittuthecoder.quiz_service.model.Quiz;
import com.bittuthecoder.quiz_service.repository.QuizRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class QuizRepositoryIntegrationTest extends BasePostgresIntegrationTest {

    @Autowired
    private QuizRepository quizRepository;

    @AfterEach
    void tearDown() {
        quizRepository.deleteAll();
    }

    @Test
    @DisplayName("Should successfully persist and find quiz by tenant and published status")
    void shouldSaveAndFilterPublishedQuizzes() {
        UUID orgId = UUID.randomUUID();

        Quiz publishedQuiz = Quiz.builder()
                .orgId(orgId)
                .title("Java Core Assessment")
                .description("Assessment covering OOP, Collections, and Concurrency")
                .published(true)
                .createdBy("instructor@assessify.io")
                .createdAt(LocalDateTime.now())
                .build();

        Quiz draftQuiz = Quiz.builder()
                .orgId(orgId)
                .title("Spring Cloud Draft")
                .description("In-progress quiz on microservices")
                .published(false)
                .createdBy("instructor@assessify.io")
                .createdAt(LocalDateTime.now())
                .build();

        quizRepository.save(publishedQuiz);
        quizRepository.save(draftQuiz);

        List<Quiz> published = quizRepository.findByOrgIdAndPublishedTrue(orgId);
        assertThat(published).hasSize(1);
        assertThat(published.get(0).getTitle()).isEqualTo("Java Core Assessment");
        assertThat(published.get(0).isPublished()).isTrue();

        Optional<Quiz> retrieved = quizRepository.findByIdAndOrgId(publishedQuiz.getId(), orgId);
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getCreatedBy()).isEqualTo("instructor@assessify.io");
    }

    @Test
    @DisplayName("Cross-tenant isolation: Tenant A cannot see quizzes owned by Tenant B")
    void crossTenantIsolationTest() {
        UUID orgA = UUID.randomUUID();
        UUID orgB = UUID.randomUUID();

        Quiz quizA = Quiz.builder()
                .orgId(orgA)
                .title("Org A Secret Exam")
                .description("Restricted exam for Org A")
                .published(true)
                .createdBy("admin@orga.com")
                .createdAt(LocalDateTime.now())
                .build();

        Quiz quizB = Quiz.builder()
                .orgId(orgB)
                .title("Org B Public Exam")
                .description("Exam for Org B")
                .published(true)
                .createdBy("admin@orgb.com")
                .createdAt(LocalDateTime.now())
                .build();

        quizRepository.save(quizA);
        quizRepository.save(quizB);

        // Org A querying quizzes
        List<Quiz> orgAQuizzes = quizRepository.findByOrgId(orgA);
        assertThat(orgAQuizzes).hasSize(1);
        assertThat(orgAQuizzes.get(0).getTitle()).isEqualTo("Org A Secret Exam");

        // Org A cannot retrieve Org B's quiz by ID
        Optional<Quiz> crossTenantAccess = quizRepository.findByIdAndOrgId(quizB.getId(), orgA);
        assertThat(crossTenantAccess).isEmpty();

        // Org B querying published quizzes
        List<Quiz> orgBPublished = quizRepository.findByOrgIdAndPublishedTrue(orgB);
        assertThat(orgBPublished).hasSize(1);
        assertThat(orgBPublished.get(0).getTitle()).isEqualTo("Org B Public Exam");
    }
}
