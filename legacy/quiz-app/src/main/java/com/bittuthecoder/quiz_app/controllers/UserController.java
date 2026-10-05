package com.bittuthecoder.quiz_app.controllers;

import com.bittuthecoder.quiz_app.models.UserModel;
import com.bittuthecoder.quiz_app.repository.UserRepository;
import com.bittuthecoder.quiz_app.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
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
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserModel> getAllUsers(Authentication authentication) {
        System.out.println("AUTH = " + authentication);
        System.out.println("AUTHORITIES = " + authentication.getAuthorities());

        return userService.getAllUsers();
    }


    @GetMapping("/users/{id}")
    public UserModel getUser(@PathVariable UUID id) {
        return userService.getUserById(id);
    }

    @GetMapping("/user")
    public UserModel getUser(Authentication authentication) {
        String email;
        Object principal = authentication.getPrincipal();

        if (principal instanceof OAuth2User) {
            email = ((OAuth2User) principal).getAttribute("email");
        } else {
            email = ((UserDetails) principal).getUsername();
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
