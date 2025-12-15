package com.bittuthecoder.question_service.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OptionRequest {

    @NotBlank
    private String text;

    private boolean correct;
}
