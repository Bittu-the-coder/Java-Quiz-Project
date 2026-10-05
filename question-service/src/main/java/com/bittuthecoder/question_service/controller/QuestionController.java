package com.bittuthecoder.question_service.controller;

import com.bittuthecoder.common.context.TenantContext;
import com.bittuthecoder.common.error.ForbiddenException;
import com.bittuthecoder.common.error.UnauthorizedException;
import com.bittuthecoder.question_service.dto.AdminQuestionResponse;
import com.bittuthecoder.question_service.dto.CandidateQuestionResponse;
import com.bittuthecoder.question_service.dto.CreateQuestionRequest;
import com.bittuthecoder.question_service.model.Option;
import com.bittuthecoder.question_service.model.Question;
import com.bittuthecoder.question_service.repository.OptionRepository;
import com.bittuthecoder.question_service.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;
    private final OptionRepository optionRepository;
    private final com.bittuthecoder.question_service.service.DeterministicPaperGenerator paperGenerator;

    private UUID getRequiredOrgId() {
        UUID orgId = TenantContext.getOrgId();
        if (orgId == null) {
            throw new UnauthorizedException("Tenant context (orgId) is required");
        }
        return orgId;
    }

    private void checkAdminAccess(String role) {
        if (role == null || !role.toUpperCase().contains("ADMIN")) {
            throw new ForbiddenException("Admin access required");
        }
    }

    // ADMIN only (author question with answer key)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminQuestionResponse createQuestion(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestBody @Valid CreateQuestionRequest request
    ) {
        checkAdminAccess(role);
        UUID orgId = getRequiredOrgId();

        Question question = Question.builder()
                .quizId(request.getQuizId())
                .text(request.getText())
                .difficulty(request.getDifficulty() != null ? request.getDifficulty() : com.bittuthecoder.question_service.model.Difficulty.MEDIUM)
                .category(request.getCategory())
                .tags(request.getTags())
                .marks(request.getMarks() != null ? request.getMarks() : 1)
                .negativeMarks(request.getNegativeMarks() != null ? request.getNegativeMarks() : 0.00)
                .build();

        List<Option> options = request.getOptions().stream()
                .map(o -> Option.builder()
                        .text(o.getText())
                        .correct(o.isCorrect())
                        .question(question)
                        .build())
                .toList();

        question.setOptions(options);

        return questionService.createQuestion(question, orgId);
    }

    // CANDIDATE + ADMIN: Candidate view (Answer key strictly omitted - Fixes Defect #1)
    @GetMapping("/quiz/{quizId}")
    public List<CandidateQuestionResponse> getQuestionsForCandidate(@PathVariable UUID quizId) {
        UUID orgId = getRequiredOrgId();
        return questionService.getCandidateQuestionsByQuiz(quizId, orgId);
    }

    // CANDIDATE: Deterministic candidate-specific paper shuffling (anti-cheating + reconnect safe)
    @GetMapping("/quiz/{quizId}/paper")
    public List<CandidateQuestionResponse> getDeterministicPaper(
            @PathVariable UUID quizId,
            @RequestParam UUID attemptId
    ) {
        UUID orgId = getRequiredOrgId();
        List<CandidateQuestionResponse> questions = questionService.getCandidateQuestionsByQuiz(quizId, orgId);
        return paperGenerator.generatePaper(questions, attemptId, quizId);
    }

    // ADMIN ONLY: View questions with answer key for exam review
    @GetMapping("/quiz/{quizId}/admin")
    public List<AdminQuestionResponse> getQuestionsForAdmin(
            @PathVariable UUID quizId,
            @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        checkAdminAccess(role);
        UUID orgId = getRequiredOrgId();
        return questionService.getAdminQuestionsByQuiz(quizId, orgId);
    }

    // CANDIDATE: Get single question (Answer key strictly omitted)
    @GetMapping("/{id}")
    public CandidateQuestionResponse getQuestionById(@PathVariable UUID id) {
        UUID orgId = getRequiredOrgId();
        return questionService.getCandidateQuestionById(id, orgId);
    }

    // INTERNAL USE ONLY: Called by result-service for server-authoritative grading.
    // External access is blocked at the API Gateway level (Fixes Defect #2).
    @PostMapping("/validate")
    public Map<UUID, Boolean> validateAnswers(
            @RequestBody Map<UUID, UUID> answers
    ) {
        UUID orgId = TenantContext.getOrgId();
        Map<UUID, Boolean> result = new HashMap<>();

        answers.forEach((questionId, optionId) -> {
            boolean correct;
            if (orgId != null) {
                correct = optionRepository.existsByIdAndQuestionIdAndOrgIdAndCorrectTrue(
                        optionId, questionId, orgId
                );
            } else {
                correct = optionRepository.existsByIdAndQuestionIdAndCorrectTrue(
                        optionId, questionId
                );
            }
            result.put(questionId, correct);
        });

        return result;
    }
}
