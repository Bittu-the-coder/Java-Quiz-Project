package com.bittuthecoder.authservice.services;

import com.bittuthecoder.authservice.dtos.*;

import java.util.List;
import java.util.UUID;

public interface OrganizationService {
    OrgResponse createOrganization(String userEmail, CreateOrgRequest request);
    List<OrgResponse> getUserOrganizations(String userEmail);
    void inviteMember(UUID orgId, String callerEmail, InviteMemberRequest request);
    SwitchOrgResponse switchOrganization(String userEmail, UUID targetOrgId);
}
