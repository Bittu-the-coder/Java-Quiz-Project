package com.bittuthecoder.api_gateway.config;


import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class GatewaySwaggerConfig {

    @Bean
    public RouteLocator swaggerRoutes(RouteLocatorBuilder builder) {
        return builder.routes()

                .route("auth-swagger", r -> r
                        .path("/v3/api-docs/auth-service")
                        .filters(f -> f
                                .rewritePath(
                                        "/v3/api-docs/auth-service",
                                        "/v3/api-docs"
                                ))
                        .uri("lb://auth-service"))

                .route("quiz-swagger", r -> r
                        .path("/v3/api-docs/quiz-service")
                        .filters(f -> f
                                .rewritePath(
                                        "/v3/api-docs/quiz-service",
                                        "/v3/api-docs"
                                ))
                        .uri("lb://quiz-service"))

                .route("question-swagger", r -> r
                        .path("/v3/api-docs/question-service")
                        .filters(f -> f
                                .rewritePath(
                                        "/v3/api-docs/question-service",
                                        "/v3/api-docs"
                                ))
                        .uri("lb://question-service"))

                .route("result-swagger", r -> r
                        .path("/v3/api-docs/result-service")
                        .filters(f -> f
                                .rewritePath(
                                        "/v3/api-docs/result-service",
                                        "/v3/api-docs"
                                ))
                        .uri("lb://result-service"))

                .build();
    }
}
