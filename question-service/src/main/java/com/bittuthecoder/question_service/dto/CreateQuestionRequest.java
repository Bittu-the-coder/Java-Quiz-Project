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

    private com.bittuthecoder.question_service.model.Difficulty difficulty;

    private String category;

    private String tags;

    private Integer marks;

    private Double negativeMarks;

    @NotNull
    private List<OptionRequest> options;
}

