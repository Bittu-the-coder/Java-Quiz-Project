package com.bittuthecoder.quiz_service.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateQuizRequest {

    @NotBlank
    private String title;

    private String description;
}
