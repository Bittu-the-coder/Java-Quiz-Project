package com.bittuthecoder.quiz_app.services.impl;


import com.bittuthecoder.quiz_app.dtos.*;
import com.bittuthecoder.quiz_app.exception.BadRequestException;
import com.bittuthecoder.quiz_app.exception.ResourceNotFoundException;
import com.bittuthecoder.quiz_app.models.*;
import com.bittuthecoder.quiz_app.repository.*;
import com.bittuthecoder.quiz_app.services.QuizAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuizAttemptServiceImpl implements QuizAttemptService {

    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final QuizAttemptAnswerRepository answerRepository;

    @Override
    public UUID startQuiz(StartQuizRequest request) {

        QuizModel quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        if (!quiz.isActive()) {
            throw new BadRequestException("Quiz is not active");
        }

        UserModel student = userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new BadRequestException("Student not found"));

        quizAttemptRepository.findByQuizIdAndStudentId(
                quiz.getId(), student.getId()
        ).ifPresent(attempt -> {
            throw new BadRequestException("Quiz already attempted");
        });

        QuizAttemptModel attempt = QuizAttemptModel.builder()
                .quiz(quiz)
                .student(student)
                .status("STARTED")
                .startedAt(LocalDateTime.now())
                .score(0)
                .build();

        return quizAttemptRepository.save(attempt).getId();
    }

    @Override
    public QuizResultResponse submitQuiz(SubmitQuizRequest request) {

        QuizAttemptModel attempt = quizAttemptRepository.findById(request.getQuizAttemptId())
                .orElseThrow(() -> new BadRequestException("Attempt not found"));

        if ("SUBMITTED".equals(attempt.getStatus())) {
            throw new BadRequestException("Quiz already submitted");
        }

        int correct = 0;
        int total = request.getAnswers().size();

        for (SubmitAnswerRequest answerRequest : request.getAnswers()) {

            QuestionModel question = questionRepository.findById(answerRequest.getQuestionId())
                    .orElseThrow(() -> new BadRequestException("Question not found"));

            OptionsModel selectedOption = optionRepository
                    .findById(answerRequest.getSelectedOptionId())
                    .orElseThrow(() -> new BadRequestException("Option not found"));

            boolean isCorrect = selectedOption.isCorrect();

            if (isCorrect) {
                correct += question.getMarks();
            }

            QuizAttemptAnswerModel answer = QuizAttemptAnswerModel.builder()
                    .quizAttempt(attempt)
                    .question(question)
                    .selectedOption(selectedOption)
                    .isCorrect(isCorrect)
                    .build();

            answerRepository.save(answer);
        }

        attempt.setScore(correct);
        attempt.setStatus("SUBMITTED");
        attempt.setSubmittedAt(LocalDateTime.now());
        quizAttemptRepository.save(attempt);

        return QuizResultResponse.builder()
                .totalQuestions(total)
                .correctAnswers(correct)
                .score(correct)
                .build();
    }
}
