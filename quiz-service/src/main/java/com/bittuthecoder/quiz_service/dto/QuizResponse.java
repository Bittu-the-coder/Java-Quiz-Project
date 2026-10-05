package com.bittuthecoder.quiz_service.dto;

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
public class QuizResponse {

    private UUID id;
    private UUID orgId;
    private String title;
    private String description;
    private boolean published;
    private String createdBy;
    private LocalDateTime createdAt;
}
