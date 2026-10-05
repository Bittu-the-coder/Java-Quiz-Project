package com.bittuthecoder.quiz_service.controller;

import com.bittuthecoder.common.context.TenantContext;
import com.bittuthecoder.common.error.ForbiddenException;
import com.bittuthecoder.common.error.UnauthorizedException;
import com.bittuthecoder.quiz_service.dto.CreateQuizRequest;
import com.bittuthecoder.quiz_service.dto.QuizResponse;
import com.bittuthecoder.quiz_service.model.Quiz;
import com.bittuthecoder.quiz_service.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    private UUID getRequiredOrgId() {
        UUID orgId = TenantContext.getOrgId();
        if (orgId == null) {
            throw new UnauthorizedException("Tenant context (orgId) is required");
        }
        return orgId;
    }

    private void checkAdminAccess(String role) {
        if (role == null || !role.toUpperCase().contains("ADMIN")) {
            throw new ForbiddenException("Only admin can perform this operation");
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuizResponse createQuiz(
            @RequestHeader(value = "X-User-Email", required = false) String email,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @Valid @RequestBody CreateQuizRequest request
    ) {
        checkAdminAccess(role);
        UUID orgId = getRequiredOrgId();

        Quiz quiz = Quiz.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .build();

        String creatorEmail = email != null ? email : TenantContext.getUserEmail();
        return quizService.createQuiz(quiz, orgId, creatorEmail);
    }

    @GetMapping
    public List<QuizResponse> getAllQuizzes(
            @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        checkAdminAccess(role);
        UUID orgId = getRequiredOrgId();
        return quizService.getAllQuizzes(orgId);
    }

    @GetMapping("/published")
    public List<QuizResponse> getPublishedQuizzes() {
        UUID orgId = getRequiredOrgId();
        return quizService.getPublishedQuizzes(orgId);
    }

    @GetMapping("/{id}")
    public QuizResponse getQuizById(@PathVariable UUID id) {
        UUID orgId = getRequiredOrgId();
        return quizService.getQuizById(id, orgId);
    }

    @PutMapping("/{id}/publish")
    public QuizResponse publishQuiz(
            @PathVariable UUID id,
            @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        checkAdminAccess(role);
        UUID orgId = getRequiredOrgId();
        return quizService.publishQuiz(id, orgId);
    }

    // ADMIN: Bulk invite candidates for an exam
    @PostMapping("/{id}/invitations/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public com.bittuthecoder.quiz_service.dto.BulkInviteResponse bulkInviteCandidates(
            @PathVariable UUID id,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @Valid @RequestBody com.bittuthecoder.quiz_service.dto.BulkInviteRequest request
    ) {
        checkAdminAccess(role);
        UUID orgId = getRequiredOrgId();
        return quizService.bulkInviteCandidates(id, request, orgId);
    }

    // ADMIN: List invitations for a quiz
    @GetMapping("/{id}/invitations")
    public List<com.bittuthecoder.quiz_service.dto.InvitationResponse> getInvitations(
            @PathVariable UUID id,
            @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        checkAdminAccess(role);
        UUID orgId = getRequiredOrgId();
        return quizService.getInvitationsByQuiz(id, orgId);
    }

    // CANDIDATE: Validate invitation token
    @GetMapping("/invitation/{token}")
    public com.bittuthecoder.quiz_service.dto.InvitationResponse getInvitationByToken(
            @PathVariable String token
    ) {
        return quizService.getInvitationByToken(token);
    }
}
