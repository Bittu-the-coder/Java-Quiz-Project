package com.bittuthecoder.quiz_app.services.impl;

import com.bittuthecoder.quiz_app.dtos.CreateQuizRequest;
import com.bittuthecoder.quiz_app.dtos.QuizResponse;
import com.bittuthecoder.quiz_app.models.QuizModel;
import com.bittuthecoder.quiz_app.models.UserModel;
import com.bittuthecoder.quiz_app.models.enums.Role;
import com.bittuthecoder.quiz_app.repository.QuizRepository;
import com.bittuthecoder.quiz_app.repository.UserRepository;
import com.bittuthecoder.quiz_app.services.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final UserRepository userRepository;

    @Override
    public QuizResponse createQuiz(CreateQuizRequest request) {

        UserModel admin = userRepository.findById(request.getAdminId())
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (admin.getRole() != Role.ADMIN) {
            throw new RuntimeException("Only admin can create quiz");
        }

        QuizModel quiz = QuizModel.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .createdBy(admin)
                .isActive(false)
                .totalMarks(0)
                .build();

        QuizModel savedQuiz = quizRepository.save(quiz);

        return mapToResponse(savedQuiz);
    }

    @Override
    public QuizResponse activateQuiz(UUID quizId) {
        QuizModel quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        quiz.setActive(true);
        return mapToResponse(quizRepository.save(quiz));
    }

    @Override
    public List<QuizResponse> getAllQuizzes() {
        return quizRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public QuizResponse getQuizById(UUID quizId) {
        QuizModel quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        return mapToResponse(quiz);
    }

    private QuizResponse mapToResponse( QuizModel quiz) {
        return QuizResponse.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .active(quiz.isActive())
                .totalMarks(quiz.getTotalMarks())
                .build();
    }
}

