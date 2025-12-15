package com.bittuthecoder.resultservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AnswerRequest {

    @NotNull
    private UUID questionId;

    @NotNull
    private UUID selectedOptionId;
}
