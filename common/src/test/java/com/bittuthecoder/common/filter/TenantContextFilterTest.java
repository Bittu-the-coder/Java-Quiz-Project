package com.bittuthecoder.common.filter;

import com.bittuthecoder.common.context.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class TenantContextFilterTest {

    private final TenantContextFilter filter = new TenantContextFilter();

    @Test
    void shouldExtractHeadersAndBindToContextDuringExecutionAndCleanUpAfter() throws ServletException, IOException {
        UUID orgId = UUID.randomUUID();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TenantContextFilter.HEADER_ORG_ID, orgId.toString());
        request.addHeader(TenantContextFilter.HEADER_USER_ID, "user-456");
        request.addHeader(TenantContextFilter.HEADER_USER_EMAIL, "admin@tenant.com");
        request.addHeader(TenantContextFilter.HEADER_USER_ROLE, "OWNER,ADMIN");

        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean filterChainExecuted = new AtomicBoolean(false);

        FilterChain chain = (req, res) -> {
            filterChainExecuted.set(true);
            assertThat(TenantContext.getOrgId()).isEqualTo(orgId);
            assertThat(TenantContext.getUserId()).isEqualTo("user-456");
            assertThat(TenantContext.getUserEmail()).isEqualTo("admin@tenant.com");
            assertThat(TenantContext.hasRole("OWNER")).isTrue();
            assertThat(TenantContext.hasRole("ADMIN")).isTrue();
        };

        filter.doFilter(request, response, chain);

        assertThat(filterChainExecuted.get()).isTrue();
        // Crucial: Context must be cleared after filter completion
        assertThat(TenantContext.getOrgId()).isNull();
        assertThat(TenantContext.getUserId()).isNull();
        assertThat(TenantContext.getUserEmail()).isNull();
        assertThat(TenantContext.getRoles()).isEmpty();
    }
}
