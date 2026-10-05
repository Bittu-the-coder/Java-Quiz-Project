package com.bittuthecoder.api_gateway.security;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Gateway filter enforcing token validation, multi-tenant security, and route protection.
 * - Strips all inbound X-User-* and X-Org-* headers to eliminate header-spoofing attacks.
 * - Blocks external access to internal endpoints (e.g. /api/questions/validate answer oracle).
 * - Verifies RS256 JWT signatures and injects verified tenant and user identity headers downstream.
 */
@Component
@RequiredArgsConstructor
public class JwtGatewayFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    private static final List<String> SPOOFABLE_HEADERS = List.of(
            "X-Org-Id",
            "X-User-Id",
            "X-User-Email",
            "X-User-Role"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 1. Block external access to internal endpoints (Mitigating answer oracle #2)
        if (isInternalOnlyPath(path)) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        // 2. Strip all inbound X-User-* and X-Org-* headers from client request immediately
        ServerHttpRequest.Builder sanitizedRequestBuilder = exchange.getRequest().mutate();
        for (String header : SPOOFABLE_HEADERS) {
            sanitizedRequestBuilder.headers(h -> h.remove(header));
        }

        // 3. Allow public endpoints through with sanitized headers
        if (isPublicPath(path)) {
            return chain.filter(exchange.mutate().request(sanitizedRequestBuilder.build()).build());
        }

        // 4. Authenticate Bearer token with RS256 public key
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        Claims claims = jwtUtil.getClaims(token);
        String role = claims.get("role") != null ? claims.get("role").toString() : "";
        Object orgId = claims.get("org_id");
        Object userId = claims.get("user_id");

        // 5. Role-based routing gate
        if (path.startsWith("/api/auth/users") && !("ADMIN".equals(role) || "OWNER".equals(role))) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        // 6. Inject verified headers
        sanitizedRequestBuilder.header("X-User-Email", claims.getSubject());
        if (!role.isBlank()) {
            sanitizedRequestBuilder.header("X-User-Role", role);
        }
        if (orgId != null) {
            sanitizedRequestBuilder.header("X-Org-Id", orgId.toString());
        }
        if (userId != null) {
            sanitizedRequestBuilder.header("X-User-Id", userId.toString());
        }

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(sanitizedRequestBuilder.build())
                .build();

        return chain.filter(mutatedExchange);
    }

    private boolean isInternalOnlyPath(String path) {
        return path.startsWith("/api/questions/validate")
                || path.startsWith("/internal/");
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/api/auth/login")
                || path.startsWith("/api/auth/register")
                || path.startsWith("/api/auth/public-key")
                || path.startsWith("/oauth2")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui");
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
