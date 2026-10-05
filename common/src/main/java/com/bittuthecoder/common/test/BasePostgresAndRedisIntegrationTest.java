package com.bittuthecoder.common.test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Singleton Testcontainer base test class for Postgres and Redis.
 */
@SpringBootTest
@ActiveProfiles("test")
public abstract class BasePostgresAndRedisIntegrationTest {

    @ServiceConnection
    protected static final PostgreSQLContainer<?> postgres;

    protected static final GenericContainer<?> redis;

    static {
        postgres = new PostgreSQLContainer<>("postgres:16-alpine");
        postgres.start();

        redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);
        redis.start();
    }

    @DynamicPropertySource
    static void configureRedis(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }
}
