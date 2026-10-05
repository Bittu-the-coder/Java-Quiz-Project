package com.bittuthecoder.quiz_app.controllers;

import com.bittuthecoder.quiz_app.dtos.LoginRequest;
import com.bittuthecoder.quiz_app.dtos.LoginResponse;
import com.bittuthecoder.quiz_app.exception.BadRequestException;
import com.bittuthecoder.quiz_app.exception.UnauthorizedException;
import com.bittuthecoder.quiz_app.models.UserModel;
import com.bittuthecoder.quiz_app.repository.UserRepository;
import com.bittuthecoder.quiz_app.security.CustomUserDetails;
import com.bittuthecoder.quiz_app.security.JwtUtil;
import com.bittuthecoder.quiz_app.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
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
