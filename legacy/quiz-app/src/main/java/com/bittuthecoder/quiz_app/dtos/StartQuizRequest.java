package com.bittuthecoder.quiz_app.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class StartQuizRequest {

    @NotNull
    private UUID quizId;

    @NotNull
    private UUID studentId;
}
