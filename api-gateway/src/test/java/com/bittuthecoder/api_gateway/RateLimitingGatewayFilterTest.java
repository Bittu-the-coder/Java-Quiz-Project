package com.bittuthecoder.api_gateway;

import com.bittuthecoder.api_gateway.security.RateLimitingGatewayFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitingGatewayFilterTest {

    private RateLimitingGatewayFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RateLimitingGatewayFilter();
    }

    @Test
    @DisplayName("Should allow requests under rate limit threshold and inject rate limit headers")
    void shouldAllowRequestsUnderThreshold() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/quizzes")
                .remoteAddress(new InetSocketAddress("192.168.1.50", 1234))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilterChain chain = ex -> Mono.empty();

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isNull(); // Proceeded through chain
        assertThat(exchange.getResponse().getHeaders().getFirst("X-RateLimit-Limit")).isEqualTo("120");
        assertThat(exchange.getResponse().getHeaders().getFirst("X-RateLimit-Remaining")).isEqualTo("119");
    }

    @Test
    @DisplayName("Should enforce strict rate limit for sensitive auth endpoints and return HTTP 429 when exceeded")
    void shouldThrottleAuthEndpointWhenExceeded() {
        GatewayFilterChain chain = ex -> Mono.empty();
        String clientIp = "10.0.0.99";

        // Auth endpoint limit is 20 requests/minute
        for (int i = 0; i < 20; i++) {
            MockServerHttpRequest req = MockServerHttpRequest.post("/api/auth/login")
                    .header("X-Forwarded-For", clientIp)
                    .build();
            MockServerWebExchange ex = MockServerWebExchange.from(req);
            filter.filter(ex, chain).block();
            assertThat(ex.getResponse().getStatusCode()).isNull();
        }

        // 21st request should be throttled with 429 Too Many Requests
        MockServerHttpRequest blockedReq = MockServerHttpRequest.post("/api/auth/login")
                .header("X-Forwarded-For", clientIp)
                .build();
        MockServerWebExchange blockedExchange = MockServerWebExchange.from(blockedReq);
        filter.filter(blockedExchange, chain).block();

        assertThat(blockedExchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(blockedExchange.getResponse().getHeaders().getFirst("Retry-After")).isEqualTo("60");
        assertThat(blockedExchange.getResponse().getHeaders().getFirst("X-RateLimit-Remaining")).isEqualTo("0");
    }
}
