package com.bittuthecoder.authservice.controllers;

import com.bittuthecoder.authservice.exception.UnauthorizedException;
import com.bittuthecoder.authservice.models.UserModel;
import com.bittuthecoder.authservice.repository.UserRepository;
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
    private final UserRepository userRepository;

    @GetMapping("/users")
    public List<UserModel> getAllUsers(
            @RequestHeader("X-User-Role") String role
    ) {
        if (!role.equals("ADMIN")) {
            throw new UnauthorizedException("Admin access only");
        }
        return userService.getAllUsers();
    }

    @GetMapping("/users/{id}")
    public UserModel getUserById(@PathVariable UUID id) {
        return userService.getUserById(id);
    }

    @GetMapping("/user")
    public UserModel getCurrentUser(
            @RequestHeader("X-User-Email") String email
    ) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
