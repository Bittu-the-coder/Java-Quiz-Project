package com.bittuthecoder.quiz_app.dtos;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class QuestionResponse {

    private UUID id;
    private String questionText;
    private int marks;
    private List<OptionResponse> options;
}
