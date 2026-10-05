package com.bittuthecoder.question_service;

import com.bittuthecoder.common.test.BasePostgresIntegrationTest;
import com.bittuthecoder.question_service.model.Option;
import com.bittuthecoder.question_service.model.Question;
import com.bittuthecoder.question_service.repository.OptionRepository;
import com.bittuthecoder.question_service.repository.QuestionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class QuestionRepositoryIntegrationTest extends BasePostgresIntegrationTest {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private OptionRepository optionRepository;

    @AfterEach
    void tearDown() {
        questionRepository.deleteAll();
    }

    @Test
    @DisplayName("Should persist question with options and query by orgId and quizId")
    void shouldPersistQuestionWithOptions() {
        UUID orgId = UUID.randomUUID();
        UUID quizId = UUID.randomUUID();

        Question question = Question.builder()
                .orgId(orgId)
                .quizId(quizId)
                .text("Which keyword creates a thread-safe singleton in Java?")
                .build();

        Option opt1 = Option.builder().text("volatile").correct(false).question(question).build();
        Option opt2 = Option.builder().text("synchronized").correct(false).question(question).build();
        Option opt3 = Option.builder().text("enum").correct(true).question(question).build();

        question.setOptions(List.of(opt1, opt2, opt3));

        Question saved = questionRepository.save(question);
        assertThat(saved.getId()).isNotNull();

        List<Question> found = questionRepository.findByOrgIdAndQuizId(orgId, quizId);
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getText()).contains("thread-safe singleton");
        assertThat(found.get(0).getOptions()).hasSize(3);

        boolean isCorrect = optionRepository.existsByIdAndQuestionIdAndOrgIdAndCorrectTrue(
                opt3.getId(), saved.getId(), orgId
        );
        assertThat(isCorrect).isTrue();
    }

    @Test
    @DisplayName("Cross-tenant isolation: Tenant A cannot access Tenant B questions or validate across tenants")
    void crossTenantIsolationTest() {
        UUID orgA = UUID.randomUUID();
        UUID orgB = UUID.randomUUID();
        UUID sharedQuizId = UUID.randomUUID(); // In case quiz ID happens to collide

        Question qA = Question.builder()
                .orgId(orgA)
                .quizId(sharedQuizId)
                .text("Org A question")
                .build();
        Option optA = Option.builder().text("Option A").correct(true).question(qA).build();
        qA.setOptions(List.of(optA));
        questionRepository.save(qA);

        Question qB = Question.builder()
                .orgId(orgB)
                .quizId(sharedQuizId)
                .text("Org B question")
                .build();
        Option optB = Option.builder().text("Option B").correct(true).question(qB).build();
        qB.setOptions(List.of(optB));
        questionRepository.save(qB);

        // Org A querying questions
        List<Question> orgAQuestions = questionRepository.findByOrgIdAndQuizId(orgA, sharedQuizId);
        assertThat(orgAQuestions).hasSize(1);
        assertThat(orgAQuestions.get(0).getText()).isEqualTo("Org A question");

        // Org A querying by Org B's question id fails
        Optional<Question> crossTenantAccess = questionRepository.findByIdAndOrgId(qB.getId(), orgA);
        assertThat(crossTenantAccess).isEmpty();

        // Cross-tenant answer validation fails: Org A validating with Org B's opt/question
        boolean crossTenantValidation = optionRepository.existsByIdAndQuestionIdAndOrgIdAndCorrectTrue(
                optB.getId(), qB.getId(), orgA
        );
        assertThat(crossTenantValidation).isFalse();
    }
}
