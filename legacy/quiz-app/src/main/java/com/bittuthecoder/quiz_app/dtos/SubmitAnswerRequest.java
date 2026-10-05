package com.bittuthecoder.quiz_app.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SubmitAnswerRequest {

    @NotNull
    private UUID questionId;

    @NotNull
    private UUID selectedOptionId;
}

