package com.bittuthecoder.quiz_app.controllers;


import com.bittuthecoder.quiz_app.dtos.*;
import com.bittuthecoder.quiz_app.services.QuizAttemptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/attempts")
@RequiredArgsConstructor
public class QuizAttemptController {

    private final QuizAttemptService quizAttemptService;

    // STUDENT - Start Quiz
    @PostMapping("/start")
    public UUID startQuiz(@RequestBody @Valid StartQuizRequest request) {
        return quizAttemptService.startQuiz(request);
    }

    // STUDENT - Submit Quiz
    @PostMapping("/submit")
    public QuizResultResponse submitQuiz(@RequestBody @Valid SubmitQuizRequest request) {
        return quizAttemptService.submitQuiz(request);
    }
}
