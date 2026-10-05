package com.bittuthecoder.authservice.controllers;

import com.bittuthecoder.authservice.dtos.*;
import com.bittuthecoder.authservice.services.OrganizationService;
import com.bittuthecoder.common.context.TenantContext;
import com.bittuthecoder.common.dto.ApiResponse;
import com.bittuthecoder.common.error.UnauthorizedException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orgs")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    private String getAuthenticatedUserEmail() {
        String email = TenantContext.getUserEmail();
        if (email == null || email.isBlank()) {
            throw new UnauthorizedException("User is not authenticated");
        }
        return email;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrgResponse>> createOrganization(
            @Valid @RequestBody CreateOrgRequest request) {
        String userEmail = getAuthenticatedUserEmail();
        OrgResponse org = organizationService.createOrganization(userEmail, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Organization created successfully", org));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<OrgResponse>>> getMyOrganizations() {
        String userEmail = getAuthenticatedUserEmail();
        List<OrgResponse> orgs = organizationService.getUserOrganizations(userEmail);
        return ResponseEntity.ok(ApiResponse.ok(orgs));
    }

    @PostMapping("/{orgId}/members")
    public ResponseEntity<ApiResponse<Void>> inviteMember(
            @PathVariable UUID orgId,
            @Valid @RequestBody InviteMemberRequest request) {
        String userEmail = getAuthenticatedUserEmail();
        organizationService.inviteMember(orgId, userEmail, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Member invited successfully", null));
    }

    @PostMapping("/switch-context")
    public ResponseEntity<ApiResponse<SwitchOrgResponse>> switchOrganization(
            @Valid @RequestBody SwitchOrgRequest request) {
        String userEmail = getAuthenticatedUserEmail();
        SwitchOrgResponse response = organizationService.switchOrganization(userEmail, request.getOrgId());
        return ResponseEntity.ok(ApiResponse.ok("Switched organization context successfully", response));
    }
}
