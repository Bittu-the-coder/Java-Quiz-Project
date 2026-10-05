package com.bittuthecoder.authservice.controllers;

import com.bittuthecoder.authservice.dtos.LoginRequest;
import com.bittuthecoder.authservice.dtos.LoginResponse;
import com.bittuthecoder.authservice.dtos.RegisterRequest;
import com.bittuthecoder.authservice.dtos.UserResponse;
import com.bittuthecoder.authservice.exception.ResourceNotFoundException;
import com.bittuthecoder.authservice.exception.UnauthorizedException;
import com.bittuthecoder.authservice.models.UserModel;
import com.bittuthecoder.authservice.repository.UserRepository;
import com.bittuthecoder.authservice.security.JwtUtil;
import com.bittuthecoder.authservice.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest request) {
        UserModel user = userService.loginUser(
                request.getEmail(),
                request.getPassword()
        );

        if (!user.isActive()) {
            throw new UnauthorizedException("User account is disabled");
        }

        String role = user.getRole() != null ? user.getRole().name() : "STUDENT";
        String token = jwtUtil.generateToken(
                user.getId(),
                user.getEmail(),
                null,
                role
        );

        return new LoginResponse(
                token,
                role,
                user.getEmail(),
                user.getName(),
                user.isActive()
        );
    }

    @GetMapping("/oauth2/success")
    public LoginResponse oauth2Success(Authentication authentication) {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");

        UserModel user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        String role = user.getRole() != null ? user.getRole().name() : "STUDENT";
        String token = jwtUtil.generateToken(
                user.getId(),
                user.getEmail(),
                null,
                role
        );

        return new LoginResponse(
                token,
                role,
                user.getEmail(),
                user.getName(),
                user.isActive()
        );
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@RequestBody @Valid RegisterRequest request) {
        return userService.registerUser(request);
    }

    @GetMapping("/public-key")
    public Map<String, String> getPublicKey() {
        return Map.of(
                "algorithm", "RS256",
                "publicKey", jwtUtil.getPublicKeyPem()
        );
    }
}
