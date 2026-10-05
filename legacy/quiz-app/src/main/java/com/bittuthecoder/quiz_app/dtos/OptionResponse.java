package com.bittuthecoder.quiz_app.dtos;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class OptionResponse {

    private UUID id;
    private String optionText;
}
