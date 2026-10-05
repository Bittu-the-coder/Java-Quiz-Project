package com.bittuthecoder.api_gateway;

import com.bittuthecoder.api_gateway.security.JwtGatewayFilter;
import com.bittuthecoder.api_gateway.security.JwtUtil;
import com.bittuthecoder.common.security.RsaKeyUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.PrivateKey;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class JwtGatewayFilterTest {

    private JwtUtil jwtUtil;
    private JwtGatewayFilter filter;
    private PrivateKey privateKey;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(RsaKeyUtil.DEFAULT_PUBLIC_KEY_PEM);
        filter = new JwtGatewayFilter(jwtUtil);
        privateKey = RsaKeyUtil.parsePrivateKey(RsaKeyUtil.DEFAULT_PRIVATE_KEY_PEM);
    }

    private String createValidRs256Token(String email, String role, UUID orgId, String userId) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .claim("org_id", orgId != null ? orgId.toString() : null)
                .claim("user_id", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    @Test
    @DisplayName("Should strip inbound spoofed headers and inject verified JWT claims using RS256")
    void shouldStripSpoofedHeadersAndInjectVerifiedClaims() {
        UUID legitimateOrgId = UUID.randomUUID();
        String token = createValidRs256Token("candidate@assessify.io", "CANDIDATE", legitimateOrgId, "usr-999");

        // Attacker attempts to spoof an admin role and different org ID in the HTTP headers
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/quizzes")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header("X-Org-Id", "spoofed-org-123")
                .header("X-User-Role", "ADMIN")
                .header("X-User-Email", "spoofed@admin.com")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        AtomicReference<HttpHeaders> downstreamHeaders = new AtomicReference<>();

        GatewayFilterChain chain = ex -> {
            downstreamHeaders.set(ex.getRequest().getHeaders());
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        HttpHeaders headers = downstreamHeaders.get();
        assertThat(headers).isNotNull();
        // Attacker's spoofed values MUST be overwritten by verified JWT claims
        assertThat(headers.getFirst("X-Org-Id")).isEqualTo(legitimateOrgId.toString());
        assertThat(headers.getFirst("X-User-Role")).isEqualTo("CANDIDATE");
        assertThat(headers.getFirst("X-User-Email")).isEqualTo("candidate@assessify.io");
        assertThat(headers.getFirst("X-User-Id")).isEqualTo("usr-999");
    }

    @Test
    @DisplayName("Should block external access to internal endpoints (Answer Oracle #2)")
    void shouldBlockExternalAccessToAnswerOracle() {
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/questions/validate")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilterChain chain = ex -> Mono.empty();
        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Should reject request without Bearer token on protected endpoints")
    void shouldRejectRequestWithoutToken() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/quizzes").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilterChain chain = ex -> Mono.empty();
        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
