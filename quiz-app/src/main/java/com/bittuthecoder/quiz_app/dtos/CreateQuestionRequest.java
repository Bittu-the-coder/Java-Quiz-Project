package com.bittuthecoder.quiz_app.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateQuestionRequest {

    @NotNull(message = "Quiz ID is required")
    private UUID quizId;

    @NotBlank(message = "Question text cannot be empty")
    private String questionText;


    private int marks;
}
