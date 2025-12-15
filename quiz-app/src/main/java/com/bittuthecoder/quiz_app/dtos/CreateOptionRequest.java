package com.bittuthecoder.quiz_app.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateOptionRequest {

    @NotNull
    private UUID questionId;

    @NotBlank
    private String optionText;

    @NotNull
    private Boolean isCorrect;
}
