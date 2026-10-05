package com.bittuthecoder.question_service;

import com.bittuthecoder.common.test.BasePostgresIntegrationTest;
import com.bittuthecoder.question_service.dto.AdminQuestionResponse;
import com.bittuthecoder.question_service.dto.CandidateOptionResponse;
import com.bittuthecoder.question_service.dto.CandidateQuestionResponse;
import com.bittuthecoder.question_service.model.Option;
import com.bittuthecoder.question_service.model.Question;
import com.bittuthecoder.question_service.repository.QuestionRepository;
import com.bittuthecoder.question_service.service.QuestionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class QuestionSecurityIntegrationTest extends BasePostgresIntegrationTest {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private QuestionRepository questionRepository;

    @AfterEach
    void tearDown() {
        questionRepository.deleteAll();
    }

    @Test
    @DisplayName("Security: Candidate response must NEVER leak answer key (Option.correct field)")
    void candidateQuestionsMustNeverLeakAnswerKey() {
        UUID orgId = UUID.randomUUID();
        UUID quizId = UUID.randomUUID();

        Question question = Question.builder()
                .orgId(orgId)
                .quizId(quizId)
                .text("What is the time complexity of HashMap.get in average case?")
                .build();

        Option opt1 = Option.builder().text("O(1)").correct(true).question(question).build();
        Option opt2 = Option.builder().text("O(n)").correct(false).question(question).build();
        Option opt3 = Option.builder().text("O(log n)").correct(false).question(question).build();
        question.setOptions(List.of(opt1, opt2, opt3));

        questionService.createQuestion(question, orgId);

        // 1. Candidate query: Verify candidate DTO does not contain 'correct' attribute
        List<CandidateQuestionResponse> candidateQuestions =
                questionService.getCandidateQuestionsByQuiz(quizId, orgId);

        assertThat(candidateQuestions).hasSize(1);
        assertThat(candidateQuestions.get(0).getOptions()).hasSize(3);

        // Introspect CandidateOptionResponse class structure: must not contain 'correct' field
        assertThat(CandidateOptionResponse.class.getDeclaredFields())
                .extracting("name")
                .doesNotContain("correct", "isCorrect");

        // 2. Admin query: Verify admin response contains 'correct' flag for exam authoring
        List<AdminQuestionResponse> adminQuestions =
                questionService.getAdminQuestionsByQuiz(quizId, orgId);

        assertThat(adminQuestions).hasSize(1);
        assertThat(adminQuestions.get(0).getOptions()).hasSize(3);
        assertThat(adminQuestions.get(0).getOptions().get(0).isCorrect()).isTrue();
        assertThat(adminQuestions.get(0).getOptions().get(1).isCorrect()).isFalse();
    }
}
