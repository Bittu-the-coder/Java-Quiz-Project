package com.bittuthecoder.common.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TenantContextTest {

    @BeforeEach
    @AfterEach
    void cleanup() {
        TenantContext.clear();
    }

    @Test
    void testTenantContextIsolationAndStorage() {
        UUID orgId = UUID.randomUUID();
        String userId = "user-123";
        String email = "candidate@example.com";
        Set<String> roles = Set.of("ORG_ADMIN", "INSTRUCTOR");

        TenantContext.setOrgId(orgId);
        TenantContext.setUserId(userId);
        TenantContext.setUserEmail(email);
        TenantContext.setRoles(roles);

        assertThat(TenantContext.getOrgId()).isEqualTo(orgId);
        assertThat(TenantContext.getUserId()).isEqualTo(userId);
        assertThat(TenantContext.getUserEmail()).isEqualTo(email);
        assertThat(TenantContext.getRoles()).containsExactlyInAnyOrder("ORG_ADMIN", "INSTRUCTOR");
        assertThat(TenantContext.hasRole("ORG_ADMIN")).isTrue();
        assertThat(TenantContext.hasRole("STUDENT")).isFalse();

        TenantContext.clear();

        assertThat(TenantContext.getOrgId()).isNull();
        assertThat(TenantContext.getUserId()).isNull();
        assertThat(TenantContext.getUserEmail()).isNull();
        assertThat(TenantContext.getRoles()).isEmpty();
    }
}
