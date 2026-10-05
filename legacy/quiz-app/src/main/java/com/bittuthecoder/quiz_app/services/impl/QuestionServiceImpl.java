package com.bittuthecoder.quiz_app.services.impl;

import com.bittuthecoder.quiz_app.dtos.*;
import com.bittuthecoder.quiz_app.exception.ResourceNotFoundException;
import com.bittuthecoder.quiz_app.models.OptionsModel;
import com.bittuthecoder.quiz_app.models.QuestionModel;
import com.bittuthecoder.quiz_app.models.QuizModel;
import com.bittuthecoder.quiz_app.repository.OptionRepository;
import com.bittuthecoder.quiz_app.repository.QuestionRepository;
import com.bittuthecoder.quiz_app.repository.QuizRepository;
import com.bittuthecoder.quiz_app.services.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final QuizRepository quizRepository;

    @Override
    public void addQuestion(CreateQuestionRequest request) {

        QuizModel quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        QuestionModel question = QuestionModel.builder()
                .quiz(quiz)
                .questionText(request.getQuestionText())
                .marks(request.getMarks())
                .build();

        questionRepository.save(question);
    }

    @Override
    public void addOption(CreateOptionRequest request) {

        QuestionModel question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new RuntimeException("Question not found"));

        OptionsModel option = OptionsModel.builder()
                .question(question)
                .optionText(request.getOptionText())
                .isCorrect(request.getIsCorrect())
                .build();

        optionRepository.save(option);
    }

    @Override
    public List<QuestionResponse> getQuestionsByQuiz(UUID quizId) {

        List<QuestionModel> questions = questionRepository.findByQuizId(quizId);

        return questions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private QuestionResponse mapToResponse(QuestionModel question) {

        List<OptionResponse> options = optionRepository
                .findByQuestionId(question.getId())
                .stream()
                .map(option -> OptionResponse.builder()
                        .id(option.getId())
                        .optionText(option.getOptionText())
                        .build())
                .collect(Collectors.toList());

        return QuestionResponse.builder()
                .id(question.getId())
                .questionText(question.getQuestionText())
                .marks(question.getMarks())
                .options(options)
                .build();
    }
}

