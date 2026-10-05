package com.bittuthecoder.resultservice.controller;

import com.bittuthecoder.common.context.TenantContext;
import com.bittuthecoder.common.error.ForbiddenException;
import com.bittuthecoder.common.error.UnauthorizedException;
import com.bittuthecoder.resultservice.dto.AttemptSummaryResponse;
import com.bittuthecoder.resultservice.dto.ResultResponse;
import com.bittuthecoder.resultservice.dto.SubmitQuizRequest;
import com.bittuthecoder.resultservice.dto.CohortAnalyticsResponse;
import com.bittuthecoder.resultservice.dto.ItemAnalysisResponse;
import com.bittuthecoder.resultservice.dto.LeaderboardEntryResponse;
import com.bittuthecoder.resultservice.service.GradingAndAnalyticsService;
import com.bittuthecoder.resultservice.service.ResultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/results")
@RequiredArgsConstructor
public class ResultController {

    private final ResultService resultService;
    private final GradingAndAnalyticsService gradingAndAnalyticsService;

    private UUID getRequiredOrgId() {
        UUID orgId = TenantContext.getOrgId();
        if (orgId == null) {
            throw new UnauthorizedException("Tenant context (orgId) is required");
        }
        return orgId;
    }

    // STUDENT ONLY (submit quiz)
    @PostMapping("/submit")
    @ResponseStatus(HttpStatus.CREATED)
    public ResultResponse submitQuiz(
            @RequestHeader(value = "X-User-Email", required = false) String emailHeader,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @Valid @RequestBody SubmitQuizRequest request
    ) {
        UUID orgId = getRequiredOrgId();
        String email = emailHeader != null ? emailHeader : TenantContext.getUserEmail();
        if (email == null) {
            throw new UnauthorizedException("User email context is required");
        }

        return resultService.submitQuiz(request, orgId, email);
    }

    // STUDENT: View own attempts
    @GetMapping("/my")
    public List<AttemptSummaryResponse> getMyAttempts(
            @RequestHeader(value = "X-User-Email", required = false) String emailHeader
    ) {
        UUID orgId = getRequiredOrgId();
        String email = emailHeader != null ? emailHeader : TenantContext.getUserEmail();
        if (email == null) {
            throw new UnauthorizedException("User email context is required");
        }
        return resultService.getStudentAttempts(orgId, email);
    }

    // ADMIN: View all attempts for a quiz
    @GetMapping("/quiz/{quizId}")
    public List<AttemptSummaryResponse> getQuizAttempts(
            @PathVariable UUID quizId,
            @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        if (role != null && !role.toUpperCase().contains("ADMIN")) {
            throw new ForbiddenException("Admin access required to view all quiz results");
        }
        UUID orgId = getRequiredOrgId();
        return resultService.getQuizAttempts(orgId, quizId);
    }

    // ADMIN: Cohort Analytics (mean, pass rate, min/max score)
    @GetMapping("/quiz/{quizId}/analytics")
    public CohortAnalyticsResponse getCohortAnalytics(
            @PathVariable UUID quizId,
            @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        if (role != null && !role.toUpperCase().contains("ADMIN")) {
            throw new ForbiddenException("Admin access required to view cohort analytics");
        }
        UUID orgId = getRequiredOrgId();
        return gradingAndAnalyticsService.getCohortAnalytics(quizId, orgId);
    }

    // ALL (Candidate / Admin): View Quiz Leaderboard
    @GetMapping("/quiz/{quizId}/leaderboard")
    public List<LeaderboardEntryResponse> getLeaderboard(
            @PathVariable UUID quizId
    ) {
        UUID orgId = getRequiredOrgId();
        return gradingAndAnalyticsService.getLeaderboard(quizId, orgId);
    }

    // ADMIN: Psychometric Item Analysis (Difficulty Index + Discrimination Index)
    @GetMapping("/quiz/{quizId}/item-analysis")
    public List<ItemAnalysisResponse> getItemAnalysis(
            @PathVariable UUID quizId,
            @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        if (role != null && !role.toUpperCase().contains("ADMIN")) {
            throw new ForbiddenException("Admin access required to view item analysis");
        }
        UUID orgId = getRequiredOrgId();
        return gradingAndAnalyticsService.getItemAnalysis(quizId, orgId);
    }
}

