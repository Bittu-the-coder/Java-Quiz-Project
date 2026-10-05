package com.bittuthecoder.question_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminOptionResponse {
    private UUID id;
    private String text;
    private boolean correct;
}
