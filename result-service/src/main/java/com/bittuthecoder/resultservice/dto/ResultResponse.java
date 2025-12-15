package com.bittuthecoder.resultservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResultResponse {

    private int totalQuestions;
    private int correctAnswers;
    private int score;
}
