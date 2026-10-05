package com.bittuthecoder.quiz_app.dtos;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class ValidationErrorResponse {

    private int status;
    private Map<String, String> errors;
}
