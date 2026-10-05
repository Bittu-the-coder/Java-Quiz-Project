package com.bittuthecoder.authservice.services.impl;

import com.bittuthecoder.authservice.dtos.RegisterRequest;
import com.bittuthecoder.authservice.dtos.UserResponse;
import com.bittuthecoder.authservice.exception.BadRequestException;
import com.bittuthecoder.authservice.exception.ResourceNotFoundException;
import com.bittuthecoder.authservice.exception.UnauthorizedException;
import com.bittuthecoder.authservice.models.UserModel;
import com.bittuthecoder.authservice.models.enums.AuthProvider;
import com.bittuthecoder.authservice.models.enums.Role;
import com.bittuthecoder.authservice.repository.UserRepository;
import com.bittuthecoder.authservice.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }

        UserModel user = UserModel.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .provider(AuthProvider.LOCAL)
                .role(Role.STUDENT) // Default safe role; administrative roles only granted via Membership
                .isActive(true)
                .build();

        UserModel saved = userRepository.save(user);
        return mapToResponse(saved);
    }

    @Override
    public UserModel loginUser(String email, String password) {
        // Uniform 401: prevents user enumeration by giving identical response regardless of whether email exists
        return userRepository.findByEmail(email)
                .filter(u -> passwordEncoder.matches(password, u.getPassword()))
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public UserResponse getUserById(UUID id) {
        return userRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private UserResponse mapToResponse(UserModel user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .provider(user.getProvider())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
