package com.bittuthecoder.authservice;

import com.bittuthecoder.authservice.dtos.LoginRequest;
import com.bittuthecoder.authservice.dtos.LoginResponse;
import com.bittuthecoder.authservice.dtos.RegisterRequest;
import com.bittuthecoder.authservice.dtos.UserResponse;
import com.bittuthecoder.authservice.exception.UnauthorizedException;
import com.bittuthecoder.authservice.repository.UserRepository;
import com.bittuthecoder.authservice.services.UserService;
import com.bittuthecoder.common.test.BasePostgresIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthSecurityIntegrationTest extends BasePostgresIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Security: Registration uses RegisterRequest DTO and never serializes password hash")
    void registrationShouldNotExposePasswordHash() {
        RegisterRequest request = RegisterRequest.builder()
                .name("Alice Security")
                .email("alice@assessify.io")
                .password("supersecret123")
                .build();

        UserResponse response = userService.registerUser(request);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getName()).isEqualTo("Alice Security");
        assertThat(response.getEmail()).isEqualTo("alice@assessify.io");

        // Ensure UserResponse class does not even have a getPassword method (DTO hygiene)
        assertThat(UserResponse.class.getDeclaredFields())
                .extracting("name")
                .doesNotContain("password", "passwordHash");
    }

    @Test
    @DisplayName("Security: Uniform 401 eliminates user enumeration between unknown email and bad password")
    void shouldEliminateUserEnumeration() {
        // Register known user
        userService.registerUser(RegisterRequest.builder()
                .name("Bob Authenticated")
                .email("bob@assessify.io")
                .password("correctPassword")
                .build());

        // Probe 1: Non-existent email -> must return "Invalid email or password"
        assertThatThrownBy(() -> userService.loginUser("nonexistent@assessify.io", "somePassword"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid email or password");

        // Probe 2: Existing email with wrong password -> must return IDENTICAL message
        assertThatThrownBy(() -> userService.loginUser("bob@assessify.io", "wrongPassword"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid email or password");
    }
}
