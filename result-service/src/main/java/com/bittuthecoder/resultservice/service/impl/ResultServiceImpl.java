package com.bittuthecoder.resultservice.service.impl;

import com.bittuthecoder.resultservice.dto.AnswerRequest;
import com.bittuthecoder.resultservice.dto.ResultResponse;
import com.bittuthecoder.resultservice.dto.SubmitQuizRequest;
import com.bittuthecoder.resultservice.feign.QuestionClient;
import com.bittuthecoder.resultservice.model.Answer;
import com.bittuthecoder.resultservice.model.QuizAttempt;
import com.bittuthecoder.resultservice.repository.QuizAttemptRepository;
import com.bittuthecoder.resultservice.service.ResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class ResultServiceImpl implements ResultService {

    private final QuizAttemptRepository attemptRepository;
    private final QuestionClient questionClient;

    @Override
    public ResultResponse submitQuiz(
            SubmitQuizRequest request,
            String studentEmail) {

        // 🔐 Prepare data for Question Service
        Map<UUID, UUID> answerMap = new HashMap<>();

        for (AnswerRequest a : request.getAnswers()) {
            answerMap.put(a.getQuestionId(), a.getSelectedOptionId());
        }

        // 🔥 FEIGN CALL (REAL VALIDATION)
        Map<UUID, Boolean> validationResult =
                questionClient.validateAnswers(answerMap);


        QuizAttempt attempt = QuizAttempt.builder()
                .quizId(request.getQuizId())
                .studentEmail(studentEmail)
                .submittedAt(LocalDateTime.now())
                .build();

        AtomicInteger correctCount = new AtomicInteger(0);
        List<Answer> answers = request.getAnswers().stream()
                .map(a -> {
                    boolean correct =
                            validationResult.getOrDefault(a.getQuestionId(), false);

                    if (correct) correctCount.getAndIncrement();

                    return Answer.builder()
                            .questionId(a.getQuestionId())
                            .selectedOptionId(a.getSelectedOptionId())
                            .correct(correct)
                            .attempt(attempt)
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
}
