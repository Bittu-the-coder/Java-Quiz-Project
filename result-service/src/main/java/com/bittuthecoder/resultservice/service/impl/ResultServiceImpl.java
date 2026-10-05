package com.bittuthecoder.resultservice.service.impl;

import com.bittuthecoder.common.error.ConflictException;
import com.bittuthecoder.common.error.GoneException;
import com.bittuthecoder.common.error.ResourceNotFoundException;
import com.bittuthecoder.resultservice.dto.*;
import com.bittuthecoder.resultservice.feign.QuestionClient;
import com.bittuthecoder.resultservice.model.Answer;
import com.bittuthecoder.resultservice.model.AttemptStatus;
import com.bittuthecoder.resultservice.model.QuizAttempt;
import com.bittuthecoder.resultservice.repository.AnswerRepository;
import com.bittuthecoder.resultservice.repository.QuizAttemptRepository;
import com.bittuthecoder.resultservice.service.ResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResultServiceImpl implements ResultService {

    private final QuizAttemptRepository attemptRepository;
    private final AnswerRepository answerRepository;
    private final QuestionClient questionClient;

    @Override
    @Transactional
    public StartAttemptResponse startAttempt(StartAttemptRequest request, UUID orgId, String studentEmail) {
        // Check for an existing in-progress attempt for this quiz
        Optional<QuizAttempt> existingOpt = attemptRepository
                .findFirstByOrgIdAndQuizIdAndStudentEmailAndStatusOrderByStartedAtDesc(
                        orgId, request.quizId(), studentEmail, AttemptStatus.IN_PROGRESS);

        LocalDateTime now = LocalDateTime.now();

        if (existingOpt.isPresent()) {
            QuizAttempt existing = existingOpt.get();
            if (now.isAfter(existing.getServerDeadline())) {
                // Deadline expired while away; auto-submit
                log.info("Active attempt {} has expired, auto-submitting", existing.getId());
                scoreAndFinalizeAttempt(existing, AttemptStatus.AUTO_SUBMITTED);
            } else {
                // Active session exists; idempotently resume
                long remainingSeconds = Math.max(0, Duration.between(now, existing.getServerDeadline()).toSeconds());
                return new StartAttemptResponse(
                        existing.getId(),
                        existing.getQuizId(),
                        existing.getStatus(),
                        existing.getStartedAt(),
                        existing.getServerDeadline(),
                        remainingSeconds
                );
            }
        }

        // Start a brand new attempt with server-authoritative timer
        int duration = (request.durationMinutes() != null && request.durationMinutes() > 0)
                ? request.durationMinutes()
                : 60;
        LocalDateTime startedAt = now;
        LocalDateTime deadline = startedAt.plusMinutes(duration);

        QuizAttempt attempt = QuizAttempt.builder()
                .orgId(orgId)
                .quizId(request.quizId())
                .studentEmail(studentEmail)
                .status(AttemptStatus.IN_PROGRESS)
                .startedAt(startedAt)
                .serverDeadline(deadline)
                .durationMinutes(duration)
                .score(0)
                .build();

        QuizAttempt saved = attemptRepository.save(attempt);
        long remainingSeconds = Duration.between(startedAt, deadline).toSeconds();

        return new StartAttemptResponse(
                saved.getId(),
                saved.getQuizId(),
                saved.getStatus(),
                saved.getStartedAt(),
                saved.getServerDeadline(),
                remainingSeconds
        );
    }

    @Override
    @Transactional(noRollbackFor = {GoneException.class})
    public AutosaveAnswerResponse autosaveAnswer(UUID attemptId, AutosaveAnswerRequest request, UUID orgId, String studentEmail) {
        QuizAttempt attempt = attemptRepository.findByIdAndOrgIdAndStudentEmail(attemptId, orgId, studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz attempt not found with id: " + attemptId));

        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new ConflictException("Attempt is not in progress: " + attempt.getStatus());
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(attempt.getServerDeadline())) {
            scoreAndFinalizeAttempt(attempt, AttemptStatus.AUTO_SUBMITTED);
            throw new GoneException("Exam time expired. Attempt has been auto-submitted.");
        }

        Answer answer = answerRepository.findByAttemptIdAndQuestionId(attemptId, request.questionId())
                .orElseGet(() -> Answer.builder()
                        .attempt(attempt)
                        .questionId(request.questionId())
                        .build());

        answer.setSelectedOptionId(request.selectedOptionId());
        answer.setAnsweredAt(now);
        answerRepository.save(answer);

        long remainingSeconds = Math.max(0, Duration.between(now, attempt.getServerDeadline()).toSeconds());
        return new AutosaveAnswerResponse(
                request.questionId(),
                request.selectedOptionId(),
                answer.getAnsweredAt(),
                remainingSeconds
        );
    }

    @Override
    @Transactional
    public ResumeAttemptResponse resumeAttempt(UUID attemptId, UUID orgId, String studentEmail) {
        QuizAttempt attempt = attemptRepository.findByIdAndOrgIdAndStudentEmail(attemptId, orgId, studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz attempt not found with id: " + attemptId));

        LocalDateTime now = LocalDateTime.now();
        if (attempt.getStatus() == AttemptStatus.IN_PROGRESS && now.isAfter(attempt.getServerDeadline())) {
            scoreAndFinalizeAttempt(attempt, AttemptStatus.AUTO_SUBMITTED);
        }

        List<Answer> answers = answerRepository.findByAttemptId(attemptId);
        Map<UUID, UUID> savedMap = new HashMap<>();
        for (Answer a : answers) {
            if (a.getSelectedOptionId() != null) {
                savedMap.put(a.getQuestionId(), a.getSelectedOptionId());
            }
        }

        long remainingSeconds = attempt.getStatus() == AttemptStatus.IN_PROGRESS
                ? Math.max(0, Duration.between(now, attempt.getServerDeadline()).toSeconds())
                : 0;

        return new ResumeAttemptResponse(
                attempt.getId(),
                attempt.getQuizId(),
                attempt.getStatus(),
                remainingSeconds,
                attempt.getServerDeadline(),
                savedMap
        );
    }

    @Override
    @Transactional
    public SubmitAttemptResponse submitAttempt(UUID attemptId, UUID orgId, String studentEmail) {
        QuizAttempt attempt = attemptRepository.findByIdAndOrgIdAndStudentEmail(attemptId, orgId, studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz attempt not found with id: " + attemptId));

        // Idempotent submit
        if (attempt.getStatus() == AttemptStatus.SUBMITTED || attempt.getStatus() == AttemptStatus.AUTO_SUBMITTED) {
            return new SubmitAttemptResponse(
                    attempt.getId(),
                    attempt.getQuizId(),
                    attempt.getStatus(),
                    attempt.getScore(),
                    attempt.getSubmittedAt()
            );
        }

        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new ConflictException("Attempt cannot be submitted from status: " + attempt.getStatus());
        }

        LocalDateTime now = LocalDateTime.now();
        AttemptStatus finalStatus = now.isAfter(attempt.getServerDeadline())
                ? AttemptStatus.AUTO_SUBMITTED
                : AttemptStatus.SUBMITTED;

        scoreAndFinalizeAttempt(attempt, finalStatus);

        return new SubmitAttemptResponse(
                attempt.getId(),
                attempt.getQuizId(),
                attempt.getStatus(),
                attempt.getScore(),
                attempt.getSubmittedAt()
        );
    }

    private void scoreAndFinalizeAttempt(QuizAttempt attempt, AttemptStatus finalStatus) {
        List<Answer> answers = answerRepository.findByAttemptId(attempt.getId());
        Map<UUID, UUID> answerMap = new HashMap<>();
        for (Answer a : answers) {
            if (a.getSelectedOptionId() != null) {
                answerMap.put(a.getQuestionId(), a.getSelectedOptionId());
            }
        }

        int correctCount = 0;
        if (!answerMap.isEmpty()) {
            try {
                Map<UUID, Boolean> validationResult = questionClient.validateAnswers(answerMap);
                for (Answer a : answers) {
                    boolean correct = validationResult.getOrDefault(a.getQuestionId(), false);
                    a.setCorrect(correct);
                    answerRepository.save(a);
                    if (correct) {
                        correctCount++;
                    }
                }
            } catch (Exception ex) {
                log.error("Failed to validate answers via questionClient for attempt {}", attempt.getId(), ex);
            }
        }

        attempt.setScore(correctCount);
        attempt.setStatus(finalStatus);
        attempt.setSubmittedAt(LocalDateTime.now());
        attemptRepository.save(attempt);
    }

    @Override
    @Transactional
    public ResultResponse submitQuiz(
            SubmitQuizRequest request,
            UUID orgId,
            String studentEmail) {

        // Prepare data for Question Service
        Map<UUID, UUID> answerMap = new HashMap<>();
        for (AnswerRequest a : request.getAnswers()) {
            answerMap.put(a.getQuestionId(), a.getSelectedOptionId());
        }

        // FEIGN CALL (Question validation)
        Map<UUID, Boolean> validationResult = questionClient.validateAnswers(answerMap);

        QuizAttempt attempt = QuizAttempt.builder()
                .orgId(orgId)
                .quizId(request.getQuizId())
                .studentEmail(studentEmail)
                .status(AttemptStatus.SUBMITTED)
                .startedAt(LocalDateTime.now())
                .submittedAt(LocalDateTime.now())
                .build();

        AtomicInteger correctCount = new AtomicInteger(0);
        List<Answer> answers = request.getAnswers().stream()
                .map(a -> {
                    boolean correct = validationResult.getOrDefault(a.getQuestionId(), false);
                    if (correct) {
                        correctCount.getAndIncrement();
                    }

                    return Answer.builder()
                            .questionId(a.getQuestionId())
                            .selectedOptionId(a.getSelectedOptionId())
                            .correct(correct)
                            .attempt(attempt)
                            .answeredAt(LocalDateTime.now())
                            .build();
                })
                .toList();

        attempt.setScore(correctCount.get());
        attempt.setAnswers(answers);

        attemptRepository.save(attempt);

        return new ResultResponse(
                answers.size(),
                correctCount.get(),
                correctCount.get()
        );
    }

    @Override
    public List<AttemptSummaryResponse> getStudentAttempts(UUID orgId, String studentEmail) {
        return attemptRepository.findByOrgIdAndStudentEmail(orgId, studentEmail).stream()
                .map(this::mapToSummary)
                .toList();
    }

    @Override
    public List<AttemptSummaryResponse> getQuizAttempts(UUID orgId, UUID quizId) {
        return attemptRepository.findByOrgIdAndQuizId(orgId, quizId).stream()
                .map(this::mapToSummary)
                .toList();
    }

    @Override
    public AttemptSummaryResponse getAttemptById(UUID id, UUID orgId) {
        QuizAttempt attempt = attemptRepository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz attempt not found with id: " + id));
        return mapToSummary(attempt);
    }

    private AttemptSummaryResponse mapToSummary(QuizAttempt a) {
        return AttemptSummaryResponse.builder()
                .id(a.getId())
                .orgId(a.getOrgId())
                .quizId(a.getQuizId())
                .studentEmail(a.getStudentEmail())
                .status(a.getStatus())
                .score(a.getScore())
                .startedAt(a.getStartedAt())
                .submittedAt(a.getSubmittedAt())
                .serverDeadline(a.getServerDeadline())
                .totalAnswers(a.getAnswers() != null ? a.getAnswers().size() : 0)
                .build();
    }
}

