package com.bittuthecoder.resultservice.config;

import com.bittuthecoder.common.context.TenantContext;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor feignTenantInterceptor() {
        return requestTemplate -> {
            UUID orgId = TenantContext.getOrgId();
            if (orgId != null) {
                requestTemplate.header("X-Org-Id", orgId.toString());
            }
            String userId = TenantContext.getUserId();
            if (userId != null) {
                requestTemplate.header("X-User-Id", userId);
            }
            String userEmail = TenantContext.getUserEmail();
            if (userEmail != null) {
                requestTemplate.header("X-User-Email", userEmail);
            }
            if (!TenantContext.getRoles().isEmpty()) {
                requestTemplate.header("X-User-Role", String.join(",", TenantContext.getRoles()));
            }
        };
    }
}
