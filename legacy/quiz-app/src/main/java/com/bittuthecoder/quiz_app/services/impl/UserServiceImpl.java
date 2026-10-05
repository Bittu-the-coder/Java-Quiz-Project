package com.bittuthecoder.quiz_app.services.impl;
import com.bittuthecoder.quiz_app.dtos.LoginResponse;
import com.bittuthecoder.quiz_app.exception.BadRequestException;
import com.bittuthecoder.quiz_app.models.UserModel;
import com.bittuthecoder.quiz_app.repository.UserRepository;
import com.bittuthecoder.quiz_app.security.JwtUtil;
import com.bittuthecoder.quiz_app.services.UserService;
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
    private final JwtUtil jwtUtil;

    @Override
    public UserModel registerUser(UserModel user) {
        // check if user already exit or not
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public UserModel loginUser(String email, String password) {
        UserModel user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadRequestException("Invalid password");
        }
        return user;
    }

    @Override
    public List<UserModel> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public UserModel getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("User not found"));
    }
}
