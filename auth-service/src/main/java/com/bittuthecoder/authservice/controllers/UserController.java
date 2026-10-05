package com.bittuthecoder.authservice.controllers;

import com.bittuthecoder.authservice.dtos.UserResponse;
import com.bittuthecoder.authservice.exception.UnauthorizedException;
import com.bittuthecoder.authservice.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/users")
    public List<UserResponse> getAllUsers(
            @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        if (role == null || !role.toUpperCase().contains("ADMIN")) {
            throw new UnauthorizedException("Admin access only");
        }
        return userService.getAllUsers();
    }

    @GetMapping("/users/{id}")
    public UserResponse getUserById(@PathVariable UUID id) {
        return userService.getUserById(id);
    }

    @GetMapping("/user")
    public UserResponse getCurrentUser(
            @RequestHeader(value = "X-User-Email", required = false) String email
    ) {
        if (email == null) {
            throw new UnauthorizedException("User context header required");
        }
        return userService.getUserByEmail(email);
    }
}
