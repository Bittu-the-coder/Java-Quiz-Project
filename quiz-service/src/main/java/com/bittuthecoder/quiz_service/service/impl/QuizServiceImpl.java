package com.bittuthecoder.quiz_service.service.impl;


import com.bittuthecoder.quiz_service.model.Quiz;
import com.bittuthecoder.quiz_service.repository.QuizRepository;
import com.bittuthecoder.quiz_service.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;

    @Override
    public Quiz createQuiz(Quiz quiz, String creatorEmail) {
        quiz.setCreatedBy(creatorEmail);
        quiz.setCreatedAt(LocalDateTime.now());
        quiz.setPublished(false);
        return quizRepository.save(quiz);
    }

    @Override
    public List<Quiz> getAllQuizzes() {
        return quizRepository.findAll();
    }

    @Override
    public List<Quiz> getPublishedQuizzes() {
        return quizRepository.findByPublishedTrue();
    }

    @Override
    public Quiz publishQuiz(UUID quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        quiz.setPublished(true);
        return quizRepository.save(quiz);
    }
}


