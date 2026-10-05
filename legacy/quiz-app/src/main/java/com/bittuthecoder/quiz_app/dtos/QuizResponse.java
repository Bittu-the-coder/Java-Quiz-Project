package com.bittuthecoder.quiz_app.dtos;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class QuizResponse {

    private UUID id;
    private String title;
    private String description;
    private boolean active;
    private int totalMarks;
}
