package com.bittuthecoder.quiz_app.dtos;


import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class SubmitQuizRequest {

    @NotNull
    private UUID quizAttemptId;

    @NotNull
    private List<SubmitAnswerRequest> answers;
}
