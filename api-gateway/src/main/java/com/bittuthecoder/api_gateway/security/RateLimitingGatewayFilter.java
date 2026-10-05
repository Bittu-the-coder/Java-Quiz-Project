package com.bittuthecoder.api_gateway.security;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Sliding window rate limiting filter for API Gateway.
 * Protects auth and attempt endpoints against brute-force attacks and DDOS traffic.
 * Returns standard 429 Too Many Requests with Retry-After and X-RateLimit headers.
 */
@Component
public class RateLimitingGatewayFilter implements GlobalFilter, Ordered {

    private static final int DEFAULT_MAX_REQUESTS_PER_MINUTE = 120;
    private static final int AUTH_MAX_REQUESTS_PER_MINUTE = 20;

    // Track request counts: clientKey:minuteBucket -> count
    private final Map<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> keyTimestamps = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String clientIp = resolveClientIp(exchange);

        int maxRequests = isSensitiveAuthPath(path)
                ? AUTH_MAX_REQUESTS_PER_MINUTE
                : DEFAULT_MAX_REQUESTS_PER_MINUTE;

        long currentMinute = System.currentTimeMillis() / 60000;
        String bucketKey = clientIp + ":" + pathScope(path) + ":" + currentMinute;

        // Periodic cleanup of older buckets
        cleanupOldBuckets(currentMinute);

        AtomicInteger counter = requestCounts.computeIfAbsent(bucketKey, k -> {
            keyTimestamps.put(k, currentMinute);
            return new AtomicInteger(0);
        });

        int currentRequests = counter.incrementAndGet();

        // Populate rate limit headers
        exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(maxRequests));
        exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", String.valueOf(Math.max(0, maxRequests - currentRequests)));

        if (currentRequests > maxRequests) {
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            exchange.getResponse().getHeaders().add("Retry-After", "60");
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

            byte[] bytes = """
                    {"status":429,"error":"Too Many Requests","message":"Rate limit exceeded. Please try again later."}
                    """.getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        }

        return chain.filter(exchange);
    }

    private String resolveClientIp(ServerWebExchange exchange) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        String xForwardedFor = headers.getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        if (exchange.getRequest().getRemoteAddress() != null) {
            return exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        }
        return "unknown";
    }

    private boolean isSensitiveAuthPath(String path) {
        return path.startsWith("/api/auth/login") || path.startsWith("/api/auth/register");
    }

    private String pathScope(String path) {
        if (path.startsWith("/api/auth")) return "auth";
        if (path.startsWith("/api/attempts")) return "attempt";
        return "general";
    }

    private void cleanupOldBuckets(long currentMinute) {
        if (requestCounts.size() > 5000) {
            keyTimestamps.entrySet().removeIf(entry -> {
                if (entry.getValue() < currentMinute - 2) {
                    requestCounts.remove(entry.getKey());
                    return true;
                }
                return false;
            });
        }
    }

    @Override
    public int getOrder() {
        return -2; // Execute before JwtGatewayFilter (-1)
    }
}
