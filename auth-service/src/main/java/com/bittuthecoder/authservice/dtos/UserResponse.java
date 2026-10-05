package com.bittuthecoder.authservice.dtos;

import com.bittuthecoder.authservice.models.enums.AuthProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private UUID id;
    private String name;
    private String email;
    private AuthProvider provider;
    private boolean active;
    private LocalDateTime createdAt;
}
