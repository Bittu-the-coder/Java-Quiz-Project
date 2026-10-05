package com.bittuthecoder.authservice.services.impl;

import com.bittuthecoder.authservice.dtos.*;
import com.bittuthecoder.authservice.models.Membership;
import com.bittuthecoder.authservice.models.Organization;
import com.bittuthecoder.authservice.models.UserModel;
import com.bittuthecoder.authservice.models.enums.MembershipStatus;
import com.bittuthecoder.authservice.models.enums.OrgRole;
import com.bittuthecoder.authservice.repository.MembershipRepository;
import com.bittuthecoder.authservice.repository.OrganizationRepository;
import com.bittuthecoder.authservice.repository.UserRepository;
import com.bittuthecoder.authservice.security.JwtUtil;
import com.bittuthecoder.authservice.services.OrganizationService;
import com.bittuthecoder.common.error.ConflictException;
import com.bittuthecoder.common.error.ForbiddenException;
import com.bittuthecoder.common.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public OrgResponse createOrganization(String userEmail, CreateOrgRequest request) {
        UserModel user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        if (organizationRepository.existsBySlug(request.getSlug())) {
            throw new ConflictException("Organization slug already taken: " + request.getSlug());
        }

        Organization org = Organization.builder()
                .name(request.getName())
                .slug(request.getSlug().toLowerCase().trim())
                .plan("FREE")
                .build();
        org = organizationRepository.save(org);

        Membership membership = Membership.builder()
                .orgId(org.getId())
                .userId(user.getId())
                .role(OrgRole.OWNER)
                .status(MembershipStatus.ACTIVE)
                .build();
        membershipRepository.save(membership);

        return OrgResponse.builder()
                .id(org.getId())
                .name(org.getName())
                .slug(org.getSlug())
                .plan(org.getPlan())
                .role(OrgRole.OWNER.name())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrgResponse> getUserOrganizations(String userEmail) {
        UserModel user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        List<Membership> memberships = membershipRepository.findByUserId(user.getId());
        List<OrgResponse> responses = new ArrayList<>();

        for (Membership m : memberships) {
            if (m.getStatus() == MembershipStatus.ACTIVE) {
                organizationRepository.findById(m.getOrgId()).ifPresent(org -> {
                    responses.add(OrgResponse.builder()
                            .id(org.getId())
                            .name(org.getName())
                            .slug(org.getSlug())
                            .plan(org.getPlan())
                            .role(m.getRole().name())
                            .build());
                });
            }
        }
        return responses;
    }

    @Override
    @Transactional
    public void inviteMember(UUID orgId, String callerEmail, InviteMemberRequest request) {
        UserModel caller = userRepository.findByEmail(callerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Caller not found: " + callerEmail));

        Membership callerMembership = membershipRepository.findByOrgIdAndUserId(orgId, caller.getId())
                .orElseThrow(() -> new ForbiddenException("You are not a member of this organization"));

        if (callerMembership.getRole() != OrgRole.OWNER && callerMembership.getRole() != OrgRole.ADMIN) {
            throw new ForbiddenException("Only organization OWNER or ADMIN can invite members");
        }

        UserModel targetUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User to invite not found with email: " + request.getEmail()));

        if (membershipRepository.existsByOrgIdAndUserId(orgId, targetUser.getId())) {
            throw new ConflictException("User is already a member of this organization");
        }

        Membership newMembership = Membership.builder()
                .orgId(orgId)
                .userId(targetUser.getId())
                .role(request.getRole())
                .status(MembershipStatus.ACTIVE)
                .build();
        membershipRepository.save(newMembership);
    }

    @Override
    @Transactional(readOnly = true)
    public SwitchOrgResponse switchOrganization(String userEmail, UUID targetOrgId) {
        UserModel user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        Organization org = organizationRepository.findById(targetOrgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found: " + targetOrgId));

        Membership membership = membershipRepository.findByOrgIdAndUserId(targetOrgId, user.getId())
                .orElseThrow(() -> new ForbiddenException("You are not an active member of this organization"));

        if (membership.getStatus() != MembershipStatus.ACTIVE) {
            throw new ForbiddenException("Your membership in this organization is not active: " + membership.getStatus());
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), org.getId(), membership.getRole().name());

        return SwitchOrgResponse.builder()
                .token(token)
                .orgId(org.getId())
                .orgName(org.getName())
                .orgSlug(org.getSlug())
                .role(membership.getRole().name())
                .build();
    }
}
