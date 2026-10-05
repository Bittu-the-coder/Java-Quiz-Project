package com.bittuthecoder.authservice.services;

import com.bittuthecoder.authservice.dtos.RegisterRequest;
import com.bittuthecoder.authservice.dtos.UserResponse;
import com.bittuthecoder.authservice.models.UserModel;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponse registerUser(RegisterRequest request);

    UserModel loginUser(String email, String password);

    List<UserResponse> getAllUsers();

    UserResponse getUserById(UUID id);

    UserResponse getUserByEmail(String email);
}
