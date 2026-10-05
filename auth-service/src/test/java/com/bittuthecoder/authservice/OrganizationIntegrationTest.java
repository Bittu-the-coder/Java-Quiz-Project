package com.bittuthecoder.authservice;

import com.bittuthecoder.authservice.dtos.*;
import com.bittuthecoder.authservice.models.UserModel;
import com.bittuthecoder.authservice.models.enums.AuthProvider;
import com.bittuthecoder.authservice.models.enums.OrgRole;
import com.bittuthecoder.authservice.models.enums.Role;
import com.bittuthecoder.authservice.repository.MembershipRepository;
import com.bittuthecoder.authservice.repository.OrganizationRepository;
import com.bittuthecoder.authservice.repository.UserRepository;
import com.bittuthecoder.authservice.security.JwtUtil;
import com.bittuthecoder.authservice.services.OrganizationService;
import com.bittuthecoder.common.error.ConflictException;
import com.bittuthecoder.common.error.ForbiddenException;
import com.bittuthecoder.common.test.BasePostgresIntegrationTest;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrganizationIntegrationTest extends BasePostgresIntegrationTest {

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private UserModel ownerUser;
    private UserModel instructorUser;
    private UserModel outsiderUser;

    @BeforeEach
    void setUp() {
        ownerUser = userRepository.save(UserModel.builder()
                .name("Alice Owner")
                .email("alice@acme.com")
                .provider(AuthProvider.LOCAL)
                .role(Role.ADMIN)
                .build());

        instructorUser = userRepository.save(UserModel.builder()
                .name("Bob Instructor")
                .email("bob@acme.com")
                .provider(AuthProvider.LOCAL)
                .role(Role.STUDENT)
                .build());

        outsiderUser = userRepository.save(UserModel.builder()
                .name("Charlie Outsider")
                .email("charlie@other.com")
                .provider(AuthProvider.LOCAL)
                .role(Role.STUDENT)
                .build());
    }

    @AfterEach
    void tearDown() {
        membershipRepository.deleteAll();
        organizationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should create organization, assign OWNER role and enforce slug uniqueness")
    void testCreateOrganizationAndSlugUniqueness() {
        CreateOrgRequest request = CreateOrgRequest.builder()
                .name("Acme Testing Institute")
                .slug("acme-institute")
                .build();

        OrgResponse response = organizationService.createOrganization(ownerUser.getEmail(), request);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getSlug()).isEqualTo("acme-institute");
        assertThat(response.getRole()).isEqualTo("OWNER");

        // Enforce duplicate slug rejection
        assertThatThrownBy(() -> organizationService.createOrganization(instructorUser.getEmail(), request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("Should invite member, switch organization context and mint org-scoped JWT")
    void testInviteAndSwitchContext() {
        OrgResponse org = organizationService.createOrganization(ownerUser.getEmail(), CreateOrgRequest.builder()
                .name("Stanford Online")
                .slug("stanford-online")
                .build());

        // Alice invites Bob as INSTRUCTOR
        organizationService.inviteMember(org.getId(), ownerUser.getEmail(), InviteMemberRequest.builder()
                .email(instructorUser.getEmail())
                .role(OrgRole.INSTRUCTOR)
                .build());

        // Bob lists his organizations
        List<OrgResponse> bobsOrgs = organizationService.getUserOrganizations(instructorUser.getEmail());
        assertThat(bobsOrgs).hasSize(1);
        assertThat(bobsOrgs.get(0).getRole()).isEqualTo("INSTRUCTOR");

        // Bob switches context to Stanford Online
        SwitchOrgResponse switchRes = organizationService.switchOrganization(instructorUser.getEmail(), org.getId());
        assertThat(switchRes.getOrgId()).isEqualTo(org.getId());
        assertThat(switchRes.getRole()).isEqualTo("INSTRUCTOR");
        assertThat(switchRes.getToken()).isNotBlank();

        // Verify minted JWT contains org_id and role
        Claims claims = jwtUtil.getClaims(switchRes.getToken());
        assertThat(claims.getSubject()).isEqualTo(instructorUser.getEmail());
        assertThat(claims.get("org_id")).isEqualTo(org.getId().toString());
        assertThat(claims.get("role")).isEqualTo("INSTRUCTOR");

        // Bob cannot invite members (only OWNER/ADMIN can)
        assertThatThrownBy(() -> organizationService.inviteMember(org.getId(), instructorUser.getEmail(),
                InviteMemberRequest.builder().email("outsider@acme.com").role(OrgRole.CANDIDATE).build()))
                .isInstanceOf(ForbiddenException.class);

        // Charlie (outsider) cannot switch context to an organization he is not a member of
        assertThatThrownBy(() -> organizationService.switchOrganization(outsiderUser.getEmail(), org.getId()))
                .isInstanceOf(ForbiddenException.class);
    }
}
