package com.bittuthecoder.quiz_app.services;
import com.bittuthecoder.quiz_app.models.UserModel;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserModel registerUser(UserModel user);

    UserModel loginUser(String email, String password);

    List<UserModel> getAllUsers();
    UserModel getUserById(UUID id);
}
