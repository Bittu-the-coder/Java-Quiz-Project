package com.bittuthecoder.quiz_app.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String role;
    private String email;
    private String name;
    private boolean isActive;
}
