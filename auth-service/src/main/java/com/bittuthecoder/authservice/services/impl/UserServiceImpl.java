package com.bittuthecoder.authservice.services.impl;

import com.bittuthecoder.authservice.exception.BadRequestException;
import com.bittuthecoder.authservice.models.UserModel;
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
    public UserModel registerUser(UserModel user) {
        // check if user already exit or not
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public UserModel loginUser(String email, String password) {
        System.out.println("in user service impl");
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
        System.out.println("In service impl in get user by id");
        return userRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("User not found"));
    }
}
