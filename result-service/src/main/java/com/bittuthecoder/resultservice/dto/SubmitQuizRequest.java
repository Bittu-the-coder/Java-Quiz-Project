package com.bittuthecoder.resultservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class SubmitQuizRequest {

    @NotNull
    private UUID quizId;

    @NotNull
    private List<AnswerRequest> answers;
}
