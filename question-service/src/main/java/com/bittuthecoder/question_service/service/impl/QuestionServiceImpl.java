package com.bittuthecoder.question_service.service.impl;

import com.bittuthecoder.common.error.ResourceNotFoundException;
import com.bittuthecoder.question_service.dto.AdminOptionResponse;
import com.bittuthecoder.question_service.dto.AdminQuestionResponse;
import com.bittuthecoder.question_service.dto.CandidateOptionResponse;
import com.bittuthecoder.question_service.dto.CandidateQuestionResponse;
import com.bittuthecoder.question_service.model.Option;
import com.bittuthecoder.question_service.model.Question;
import com.bittuthecoder.question_service.repository.QuestionRepository;
import com.bittuthecoder.question_service.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;

    @Override
    public AdminQuestionResponse createQuestion(Question question, UUID orgId) {
        question.setOrgId(orgId);
        if (question.getOptions() != null) {
            question.getOptions().forEach(opt -> opt.setQuestion(question));
        }
        Question saved = questionRepository.save(question);
        return mapToAdminResponse(saved);
    }

    @Override
    public List<CandidateQuestionResponse> getCandidateQuestionsByQuiz(UUID quizId, UUID orgId) {
        return questionRepository.findByOrgIdAndQuizId(orgId, quizId).stream()
                .map(this::mapToCandidateResponse)
                .toList();
    }

    @Override
    public List<AdminQuestionResponse> getAdminQuestionsByQuiz(UUID quizId, UUID orgId) {
        return questionRepository.findByOrgIdAndQuizId(orgId, quizId).stream()
                .map(this::mapToAdminResponse)
                .toList();
    }

    @Override
    public CandidateQuestionResponse getCandidateQuestionById(UUID questionId, UUID orgId) {
        Question q = questionRepository.findByIdAndOrgId(questionId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + questionId));
        return mapToCandidateResponse(q);
    }

    @Override
    public AdminQuestionResponse getAdminQuestionById(UUID questionId, UUID orgId) {
        Question q = questionRepository.findByIdAndOrgId(questionId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + questionId));
        return mapToAdminResponse(q);
    }

    private CandidateQuestionResponse mapToCandidateResponse(Question q) {
        List<CandidateOptionResponse> optionDtos = (q.getOptions() != null)
                ? q.getOptions().stream()
                .map(opt -> CandidateOptionResponse.builder()
                        .id(opt.getId())
                        .text(opt.getText())
                        .build())
                .toList()
                : Collections.emptyList();

        return CandidateQuestionResponse.builder()
                .id(q.getId())
                .quizId(q.getQuizId())
                .text(q.getText())
                .difficulty(q.getDifficulty())
                .category(q.getCategory())
                .tags(q.getTags())
                .marks(q.getMarks())
                .options(optionDtos)
                .build();
    }

    private AdminQuestionResponse mapToAdminResponse(Question q) {
        List<AdminOptionResponse> optionDtos = (q.getOptions() != null)
                ? q.getOptions().stream()
                .map(opt -> AdminOptionResponse.builder()
                        .id(opt.getId())
                        .text(opt.getText())
                        .correct(opt.isCorrect())
                        .build())
                .toList()
                : Collections.emptyList();

        return AdminQuestionResponse.builder()
                .id(q.getId())
                .quizId(q.getQuizId())
                .text(q.getText())
                .difficulty(q.getDifficulty())
                .category(q.getCategory())
                .tags(q.getTags())
                .marks(q.getMarks())
                .negativeMarks(q.getNegativeMarks())
                .options(optionDtos)
                .build();
    }
}
