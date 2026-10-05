package com.bittuthecoder.authservice;

import com.bittuthecoder.authservice.models.UserModel;
import com.bittuthecoder.authservice.models.enums.AuthProvider;
import com.bittuthecoder.authservice.models.enums.Role;
import com.bittuthecoder.authservice.repository.UserRepository;
import com.bittuthecoder.common.test.BasePostgresIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserRepositoryIntegrationTest extends BasePostgresIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should successfully persist and find user by email using Testcontainers Postgres")
    void shouldSaveAndFindUserByEmail() {
        UserModel user = UserModel.builder()
                .name("Alex Johnson")
                .email("alex@assessify.io")
                .password("$2a$10$hashedpassword")
                .provider(AuthProvider.LOCAL)
                .role(Role.ADMIN)
                .isActive(true)
                .build();

        UserModel saved = userRepository.save(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();

        Optional<UserModel> found = userRepository.findByEmail("alex@assessify.io");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Alex Johnson");
        assertThat(found.get().getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("Should enforce email uniqueness in database")
    void shouldEnforceEmailUniqueness() {
        UserModel user1 = UserModel.builder()
                .name("User One")
                .email("duplicate@assessify.io")
                .provider(AuthProvider.LOCAL)
                .role(Role.STUDENT)
                .build();
        userRepository.saveAndFlush(user1);

        UserModel user2 = UserModel.builder()
                .name("User Two")
                .email("duplicate@assessify.io")
                .provider(AuthProvider.LOCAL)
                .role(Role.STUDENT)
                .build();

        assertThatThrownBy(() -> userRepository.saveAndFlush(user2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
