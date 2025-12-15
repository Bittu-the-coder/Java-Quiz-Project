package com.bittuthecoder.question_service.controller;


import com.bittuthecoder.question_service.dto.CreateQuestionRequest;
import com.bittuthecoder.question_service.dto.OptionRequest;
import com.bittuthecoder.question_service.model.Option;
import com.bittuthecoder.question_service.model.Question;
import com.bittuthecoder.question_service.repository.OptionRepository;
import com.bittuthecoder.question_service.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;
    private final OptionRepository optionRepository;

    // ADMIN only (Gateway enforced)
    @PostMapping
    public Question createQuestion(
            @RequestHeader("X-User-Role") String role,
            @RequestBody @Valid CreateQuestionRequest request
    ) {
        if (!role.equals("ADMIN")) {
            throw new RuntimeException("Admin access only");
        }

        Question question = Question.builder()
                .quizId(request.getQuizId())
                .text(request.getText())
                .build();

        List<Option> options = request.getOptions().stream()
                .map(o -> Option.builder()
                        .text(o.getText())
                        .correct(o.isCorrect())
                        .question(question)
                        .build())
                .toList();

        question.setOptions(options);

        return questionService.createQuestion(question);
    }

    // STUDENT + ADMIN
    @GetMapping("/quiz/{quizId}")
    public List<Question> getQuestions(@PathVariable UUID quizId) {
        return questionService.getQuestionsByQuiz(quizId);
    }

    @PostMapping("/validate")
    public Map<UUID, Boolean> validateAnswers(
            @RequestBody Map<UUID, UUID> answers
    ) {
        Map<UUID, Boolean> result = new HashMap<>();

        answers.forEach((questionId, optionId) -> {
            boolean correct = optionRepository
                    .existsByIdAndQuestionIdAndCorrectTrue(
                            optionId, questionId
                    );
            result.put(questionId, correct);
        });

        return result;
    }

}
