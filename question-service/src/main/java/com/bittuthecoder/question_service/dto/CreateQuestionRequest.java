package com.bittuthecoder.question_service.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class CreateQuestionRequest {

    @NotNull
    private UUID quizId;

    @NotBlank
    private String text;

    @NotNull
    private List<OptionRequest> options;
}

