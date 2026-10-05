package com.bittuthecoder.common.error;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class ApiExceptionTest {

    @Test
    void testResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Quiz not found");
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getMessage()).isEqualTo("Quiz not found");
    }

    @Test
    void testBadRequestException() {
        BadRequestException ex = new BadRequestException("Invalid input");
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getMessage()).isEqualTo("Invalid input");
    }

    @Test
    void testUnauthorizedException() {
        UnauthorizedException ex = new UnauthorizedException("Missing token");
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(ex.getMessage()).isEqualTo("Missing token");
    }

    @Test
    void testForbiddenException() {
        ForbiddenException ex = new ForbiddenException("Access denied");
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(ex.getMessage()).isEqualTo("Access denied");
    }

    @Test
    void testConflictException() {
        ConflictException ex = new ConflictException("User already exists");
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(ex.getMessage()).isEqualTo("User already exists");
    }
}
