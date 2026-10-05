package com.bittuthecoder.common.filter;

import com.bittuthecoder.common.context.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Servlet filter that reads sanitized X-Org-Id, X-User-Id, X-User-Email, X-User-Role
 * injected by the API Gateway and binds them to the TenantContext.
 * Automatically guarantees cleanup in the finally block to prevent thread pool reuse pollution.
 */
public class TenantContextFilter extends OncePerRequestFilter {

    public static final String HEADER_ORG_ID = "X-Org-Id";
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_EMAIL = "X-User-Email";
    public static final String HEADER_USER_ROLE = "X-User-Role";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String orgIdHeader = request.getHeader(HEADER_ORG_ID);
            if (orgIdHeader != null && !orgIdHeader.isBlank()) {
                try {
                    TenantContext.setOrgId(UUID.fromString(orgIdHeader.trim()));
                } catch (IllegalArgumentException ignored) {
                    // Ignore malformed UUID
                }
            }

            String userIdHeader = request.getHeader(HEADER_USER_ID);
            if (userIdHeader != null && !userIdHeader.isBlank()) {
                TenantContext.setUserId(userIdHeader.trim());
            }

            String emailHeader = request.getHeader(HEADER_USER_EMAIL);
            if (emailHeader != null && !emailHeader.isBlank()) {
                TenantContext.setUserEmail(emailHeader.trim());
            }

            String roleHeader = request.getHeader(HEADER_USER_ROLE);
            if (roleHeader != null && !roleHeader.isBlank()) {
                String[] splitRoles = roleHeader.split(",");
                Set<String> roles = new HashSet<>();
                for (String r : splitRoles) {
                    if (!r.isBlank()) {
                        roles.add(r.trim());
                    }
                }
                TenantContext.setRoles(roles);
            }

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
