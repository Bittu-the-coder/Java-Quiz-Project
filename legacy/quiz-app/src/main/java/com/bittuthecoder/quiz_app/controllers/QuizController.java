package com.bittuthecoder.quiz_app.controllers;

import com.bittuthecoder.quiz_app.dtos.CreateQuizRequest;
import com.bittuthecoder.quiz_app.dtos.QuizResponse;
import com.bittuthecoder.quiz_app.services.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    // ADMIN - Create Quiz
    @PostMapping
    public QuizResponse createQuiz(@RequestBody @Valid CreateQuizRequest request) {
        return quizService.createQuiz(request);
    }

    // ADMIN - Activate Quiz
    @PutMapping("/{quizId}/activate")
    public QuizResponse activateQuiz(@PathVariable UUID quizId) {
        return quizService.activateQuiz(quizId);
    }

    // PUBLIC - Get all quizzes
    @GetMapping
    public List<QuizResponse> getAllQuizzes() {
        return quizService.getAllQuizzes();
    }

    // PUBLIC - Get quiz by ID
    @GetMapping("/{quizId}")
    public QuizResponse getQuizById(@PathVariable UUID quizId) {
        return quizService.getQuizById(quizId);
    }
}

