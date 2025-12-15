package com.bittuthecoder.quiz_app.dtos;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuizResultResponse {

    private int totalQuestions;
    private int correctAnswers;
    private int score;
}
