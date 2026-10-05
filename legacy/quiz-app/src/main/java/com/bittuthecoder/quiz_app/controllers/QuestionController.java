package com.bittuthecoder.quiz_app.controllers;


import com.bittuthecoder.quiz_app.dtos.CreateOptionRequest;
import com.bittuthecoder.quiz_app.dtos.CreateQuestionRequest;
import com.bittuthecoder.quiz_app.dtos.QuestionResponse;
import com.bittuthecoder.quiz_app.services.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    // ADMIN + STUDENT
    private final QuestionService questionService;

    // ADMIN - Add Question
    @PostMapping
    public void addQuestion(@RequestBody @Valid CreateQuestionRequest request) {
        questionService.addQuestion(request);
    }

    // ADMIN - Add Option
    @PostMapping("/option")
    public void addOption(@RequestBody @Valid CreateOptionRequest request) {
        questionService.addOption(request);
    }

    // STUDENT - Get Questions for Quiz
    @GetMapping("/quiz/{quizId}")
    public List<QuestionResponse> getQuestions(@PathVariable UUID quizId) {
        return questionService.getQuestionsByQuiz(quizId);
    }
}
