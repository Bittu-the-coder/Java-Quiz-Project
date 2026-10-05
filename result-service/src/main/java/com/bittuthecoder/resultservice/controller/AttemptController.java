package com.bittuthecoder.resultservice.controller;

import com.bittuthecoder.common.context.TenantContext;
import com.bittuthecoder.common.error.UnauthorizedException;
import com.bittuthecoder.resultservice.dto.*;
import com.bittuthecoder.resultservice.service.ResultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/attempts")
@RequiredArgsConstructor
public class AttemptController {

    private final ResultService resultService;
    private final com.bittuthecoder.resultservice.service.ProctorService proctorService;

    private UUID getRequiredOrgId() {
        UUID orgId = TenantContext.getOrgId();
        if (orgId == null) {
            throw new UnauthorizedException("Tenant context (orgId) is required");
        }
        return orgId;
    }

    private String getRequiredUserEmail(String emailHeader) {
        String email = emailHeader != null ? emailHeader : TenantContext.getUserEmail();
        if (email == null) {
            throw new UnauthorizedException("User email context is required");
        }
        return email;
    }

    private void checkAdminAccess(String role) {
        if (role == null || !role.toUpperCase().contains("ADMIN")) {
            throw new com.bittuthecoder.common.error.ForbiddenException("Admin access required for proctor audit review");
        }
    }

    @PostMapping("/start")
    @ResponseStatus(HttpStatus.CREATED)
    public StartAttemptResponse startAttempt(
            @RequestHeader(value = "X-User-Email", required = false) String emailHeader,
            @Valid @RequestBody StartAttemptRequest request
    ) {
        UUID orgId = getRequiredOrgId();
        String email = getRequiredUserEmail(emailHeader);
        return resultService.startAttempt(request, orgId, email);
    }

    @PutMapping("/{attemptId}/answer")
    public AutosaveAnswerResponse autosaveAnswer(
            @PathVariable UUID attemptId,
            @RequestHeader(value = "X-User-Email", required = false) String emailHeader,
            @Valid @RequestBody AutosaveAnswerRequest request
    ) {
        UUID orgId = getRequiredOrgId();
        String email = getRequiredUserEmail(emailHeader);
        return resultService.autosaveAnswer(attemptId, request, orgId, email);
    }

    @GetMapping("/{attemptId}/resume")
    public ResumeAttemptResponse resumeAttempt(
            @PathVariable UUID attemptId,
            @RequestHeader(value = "X-User-Email", required = false) String emailHeader
    ) {
        UUID orgId = getRequiredOrgId();
        String email = getRequiredUserEmail(emailHeader);
        return resultService.resumeAttempt(attemptId, orgId, email);
    }

    @PostMapping("/{attemptId}/submit")
    public SubmitAttemptResponse submitAttempt(
            @PathVariable UUID attemptId,
            @RequestHeader(value = "X-User-Email", required = false) String emailHeader
    ) {
        UUID orgId = getRequiredOrgId();
        String email = getRequiredUserEmail(emailHeader);
        return resultService.submitAttempt(attemptId, orgId, email);
    }

    // CANDIDATE: Ingest signed, monotonic proctor events
    @PostMapping("/{attemptId}/proctor-events")
    public ProctorEventResponse recordProctorEvent(
            @PathVariable UUID attemptId,
            @RequestHeader(value = "X-User-Email", required = false) String emailHeader,
            @Valid @RequestBody ProctorEventRequest request
    ) {
        UUID orgId = getRequiredOrgId();
        String email = getRequiredUserEmail(emailHeader);
        return proctorService.recordEvent(attemptId, request, orgId, email);
    }

    // ADMIN/STAFF: Review immutable proctor audit trail
    @GetMapping("/{attemptId}/proctor-events")
    public List<ProctorAuditLogResponse> getProctorAuditTrail(
            @PathVariable UUID attemptId,
            @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        checkAdminAccess(role);
        UUID orgId = getRequiredOrgId();
        return proctorService.getAuditTrail(attemptId, orgId);
    }
}
