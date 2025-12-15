package com.bittuthecoder.authservice.controllers;

import com.bittuthecoder.authservice.dtos.LoginRequest;
import com.bittuthecoder.authservice.dtos.LoginResponse;
import com.bittuthecoder.authservice.exception.UnauthorizedException;
import com.bittuthecoder.authservice.models.UserModel;
import com.bittuthecoder.authservice.repository.UserRepository;
import com.bittuthecoder.authservice.security.JwtUtil;
import com.bittuthecoder.authservice.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

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

        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole().name()
        );

        return new LoginResponse(
                token,
                user.getRole().name(),
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
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole().name()
        );

        return new LoginResponse(
                token,
                user.getRole().name(),
                user.getEmail(),
                user.getName(),
                user.isActive()
        );
    }

    @PostMapping("/register")
    public UserModel register(@RequestBody UserModel user) {
        return userService.registerUser(user);
    }
}
