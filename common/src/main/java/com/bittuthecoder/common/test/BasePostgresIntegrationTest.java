package com.bittuthecoder.common.test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Singleton Testcontainer base test class.
 * Starts a single PostgreSQL container for the entire test suite run,
 * avoiding teardown/port-refusal between separate test classes.
 */
@SpringBootTest
@ActiveProfiles("test")
public abstract class BasePostgresIntegrationTest {

    @ServiceConnection
    protected static final PostgreSQLContainer<?> postgres;

    static {
        postgres = new PostgreSQLContainer<>("postgres:16-alpine");
        postgres.start();
    }
}
