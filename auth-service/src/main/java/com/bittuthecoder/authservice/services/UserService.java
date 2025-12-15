package com.bittuthecoder.authservice.services;
import com.bittuthecoder.authservice.models.UserModel;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserModel registerUser(UserModel user);

    UserModel loginUser(String email, String password);

    List<UserModel> getAllUsers();
    UserModel getUserById(UUID id);
}
