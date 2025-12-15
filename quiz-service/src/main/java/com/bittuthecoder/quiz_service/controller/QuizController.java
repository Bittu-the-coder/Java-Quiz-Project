package com.bittuthecoder.quiz_service.controller;



import com.bittuthecoder.quiz_service.dto.CreateQuizRequest;
import com.bittuthecoder.quiz_service.model.Quiz;
import com.bittuthecoder.quiz_service.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    // ADMIN ONLY (checked by Gateway)
    @PostMapping
    public Quiz createQuiz(
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role") String role,
            @RequestBody CreateQuizRequest request
    ) {
        if (!role.equals("ADMIN")) {
            throw new RuntimeException("Only admin can create quizzes");
        }

        Quiz quiz = new Quiz();
        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());

        return quizService.createQuiz(quiz, email);
    }




    // ADMIN
    @GetMapping
    public List<Quiz> getAllQuizzes(
            @RequestHeader("X-User-Role") String role
    ) {
        if (!role.equals("ADMIN")) {
            throw new RuntimeException("Admin access only");
        }
        return quizService.getAllQuizzes();
    }

    // STUDENT
    @GetMapping("/published")
    public List<Quiz> getPublishedQuizzes() {
        return quizService.getPublishedQuizzes();
    }

    // ADMIN
    @PutMapping("/{id}/publish")
    public Quiz publishQuiz(
            @PathVariable UUID id,
            @RequestHeader("X-User-Role") String role
    ) {
        if (!role.equals("ADMIN")) {
            throw new RuntimeException("Admin access only");
        }
        return quizService.publishQuiz(id);
    }
}
