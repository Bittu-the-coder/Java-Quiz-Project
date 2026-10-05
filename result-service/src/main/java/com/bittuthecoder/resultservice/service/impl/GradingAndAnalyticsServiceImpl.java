package com.bittuthecoder.resultservice.service.impl;

import com.bittuthecoder.resultservice.dto.CohortAnalyticsResponse;
import com.bittuthecoder.resultservice.dto.ItemAnalysisResponse;
import com.bittuthecoder.resultservice.dto.LeaderboardEntryResponse;
import com.bittuthecoder.resultservice.model.Answer;
import com.bittuthecoder.resultservice.model.AttemptStatus;
import com.bittuthecoder.resultservice.model.ExamResult;
import com.bittuthecoder.resultservice.model.ItemAnalysis;
import com.bittuthecoder.resultservice.model.QuizAttempt;
import com.bittuthecoder.resultservice.repository.AnswerRepository;
import com.bittuthecoder.resultservice.repository.ExamResultRepository;
import com.bittuthecoder.resultservice.repository.ItemAnalysisRepository;
import com.bittuthecoder.resultservice.repository.QuizAttemptRepository;
import com.bittuthecoder.resultservice.service.GradingAndAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GradingAndAnalyticsServiceImpl implements GradingAndAnalyticsService {

    private final QuizAttemptRepository attemptRepository;
    private final AnswerRepository answerRepository;
    private final ExamResultRepository examResultRepository;
    private final ItemAnalysisRepository itemAnalysisRepository;

    @Override
    @Transactional
    public void calculateAndPersistExamResults(UUID quizId, UUID orgId) {
        List<QuizAttempt> allAttempts = attemptRepository.findByOrgIdAndQuizId(orgId, quizId);
        List<QuizAttempt> completed = allAttempts.stream()
                .filter(a -> a.getStatus() == AttemptStatus.SUBMITTED || a.getStatus() == AttemptStatus.AUTO_SUBMITTED)
                .sorted(Comparator.comparingInt(QuizAttempt::getScore).reversed()
                        .thenComparing(QuizAttempt::getSubmittedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        if (completed.isEmpty()) {
            return;
        }

        int totalCount = completed.size();

        for (int i = 0; i < totalCount; i++) {
            QuizAttempt attempt = completed.get(i);
            int rank = i + 1;
            double percentile = totalCount > 1
                    ? Math.round((((double) (totalCount - rank) / (totalCount - 1)) * 100.0) * 100.0) / 100.0
                    : 100.0;

            List<Answer> answers = answerRepository.findByAttemptId(attempt.getId());
            double maxScore = Math.max(1.0, answers.size());
            double percentage = Math.round(((attempt.getScore() / maxScore) * 100.0) * 100.0) / 100.0;
            boolean passed = percentage >= 50.0;

            ExamResult result = examResultRepository.findByAttemptId(attempt.getId())
                    .orElseGet(() -> ExamResult.builder()
                            .orgId(orgId)
                            .quizId(quizId)
                            .attemptId(attempt.getId())
                            .studentEmail(attempt.getStudentEmail())
                            .build());

            result.setTotalScore(attempt.getScore());
            result.setMaxPossibleScore(maxScore);
            result.setPercentage(percentage);
            result.setRankInExam(rank);
            result.setPercentile(percentile);
            result.setPassed(passed);

            examResultRepository.save(result);
        }

        // Compute Item Analysis (Difficulty and Discrimination Index)
        Map<UUID, List<Answer>> answersByQuestion = completed.stream()
                .flatMap(a -> answerRepository.findByAttemptId(a.getId()).stream())
                .collect(Collectors.groupingBy(Answer::getQuestionId));

        int cohortTopIndex = Math.max(1, (int) Math.ceil(totalCount * 0.27));
        Set<UUID> topAttemptIds = completed.stream().limit(cohortTopIndex).map(QuizAttempt::getId).collect(Collectors.toSet());
        Set<UUID> bottomAttemptIds = completed.stream().skip(Math.max(0, totalCount - cohortTopIndex)).map(QuizAttempt::getId).collect(Collectors.toSet());

        answersByQuestion.forEach((questionId, answers) -> {
            int total = answers.size();
            int correct = (int) answers.stream().filter(Answer::isCorrect).count();
            double difficulty = total > 0 ? Math.round(((double) correct / total) * 10000.0) / 10000.0 : 0.0;

            long topCorrect = answers.stream().filter(a -> topAttemptIds.contains(a.getAttempt().getId()) && a.isCorrect()).count();
            long bottomCorrect = answers.stream().filter(a -> bottomAttemptIds.contains(a.getAttempt().getId()) && a.isCorrect()).count();
            double topRate = !topAttemptIds.isEmpty() ? (double) topCorrect / topAttemptIds.size() : 0.0;
            double bottomRate = !bottomAttemptIds.isEmpty() ? (double) bottomCorrect / bottomAttemptIds.size() : 0.0;
            double discrimination = Math.round((topRate - bottomRate) * 10000.0) / 10000.0;

            ItemAnalysis item = itemAnalysisRepository.findByQuizIdAndQuestionId(quizId, questionId)
                    .orElseGet(() -> ItemAnalysis.builder()
                            .orgId(orgId)
                            .quizId(quizId)
                            .questionId(questionId)
                            .build());

            item.setTotalAttempts(total);
            item.setCorrectAttempts(correct);
            item.setDifficultyIndex(difficulty);
            item.setDiscriminationIndex(discrimination);

            itemAnalysisRepository.save(item);
        });
    }

    @Override
    public CohortAnalyticsResponse getCohortAnalytics(UUID quizId, UUID orgId) {
        calculateAndPersistExamResults(quizId, orgId);
        List<ExamResult> results = examResultRepository.findByOrgIdAndQuizIdOrderByTotalScoreDesc(orgId, quizId);

        if (results.isEmpty()) {
            return new CohortAnalyticsResponse(quizId, 0, 0.0, 0.0, 0.0, 0.0);
        }

        double totalScoreSum = 0.0;
        double maxScore = Double.MIN_VALUE;
        double minScore = Double.MAX_VALUE;
        int passCount = 0;

        for (ExamResult r : results) {
            totalScoreSum += r.getTotalScore();
            if (r.getTotalScore() > maxScore) maxScore = r.getTotalScore();
            if (r.getTotalScore() < minScore) minScore = r.getTotalScore();
            if (r.isPassed()) passCount++;
        }

        double avg = Math.round((totalScoreSum / results.size()) * 100.0) / 100.0;
        double passRate = Math.round(((double) passCount / results.size()) * 100.0 * 100.0) / 100.0;

        return new CohortAnalyticsResponse(
                quizId,
                results.size(),
                avg,
                maxScore,
                minScore,
                passRate
        );
    }

    @Override
    public List<LeaderboardEntryResponse> getLeaderboard(UUID quizId, UUID orgId) {
        calculateAndPersistExamResults(quizId, orgId);
        return examResultRepository.findByOrgIdAndQuizIdOrderByTotalScoreDesc(orgId, quizId).stream()
                .map(r -> new LeaderboardEntryResponse(
                        r.getRankInExam() != null ? r.getRankInExam() : 1,
                        r.getStudentEmail(),
                        r.getTotalScore(),
                        r.getPercentage(),
                        r.getPercentile() != null ? r.getPercentile() : 100.0,
                        r.isPassed()
                ))
                .toList();
    }

    @Override
    public List<ItemAnalysisResponse> getItemAnalysis(UUID quizId, UUID orgId) {
        calculateAndPersistExamResults(quizId, orgId);
        return itemAnalysisRepository.findByOrgIdAndQuizId(orgId, quizId).stream()
                .map(item -> {
                    String rating;
                    if (item.getDifficultyIndex() >= 0.75) {
                        rating = "EASY";
                    } else if (item.getDifficultyIndex() <= 0.35) {
                        rating = "HARD";
                    } else {
                        rating = "MODERATE";
                    }

                    return new ItemAnalysisResponse(
                            item.getQuestionId(),
                            item.getTotalAttempts(),
                            item.getCorrectAttempts(),
                            item.getDifficultyIndex(),
                            item.getDiscriminationIndex(),
                            rating
                    );
                })
                .toList();
    }
}
