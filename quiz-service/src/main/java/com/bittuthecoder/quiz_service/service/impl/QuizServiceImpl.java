package com.bittuthecoder.quiz_service.service.impl;

import com.bittuthecoder.common.error.ResourceNotFoundException;
import com.bittuthecoder.quiz_service.dto.BulkInviteRequest;
import com.bittuthecoder.quiz_service.dto.BulkInviteResponse;
import com.bittuthecoder.quiz_service.dto.InvitationResponse;
import com.bittuthecoder.quiz_service.dto.QuizResponse;
import com.bittuthecoder.quiz_service.model.Quiz;
import com.bittuthecoder.quiz_service.model.QuizInvitation;
import com.bittuthecoder.quiz_service.repository.QuizInvitationRepository;
import com.bittuthecoder.quiz_service.repository.QuizRepository;
import com.bittuthecoder.quiz_service.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final QuizInvitationRepository quizInvitationRepository;

    @Override
    public QuizResponse createQuiz(Quiz quiz, UUID orgId, String creatorEmail) {
        quiz.setOrgId(orgId);
        quiz.setCreatedBy(creatorEmail);
        quiz.setCreatedAt(LocalDateTime.now());
        quiz.setPublished(false);
        Quiz saved = quizRepository.save(quiz);
        return mapToResponse(saved);
    }

    @Override
    public List<QuizResponse> getAllQuizzes(UUID orgId) {
        return quizRepository.findByOrgId(orgId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<QuizResponse> getPublishedQuizzes(UUID orgId) {
        return quizRepository.findByOrgIdAndPublishedTrue(orgId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public QuizResponse publishQuiz(UUID quizId, UUID orgId) {
        Quiz quiz = quizRepository.findByIdAndOrgId(quizId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + quizId));
        quiz.setPublished(true);
        Quiz saved = quizRepository.save(quiz);
        return mapToResponse(saved);
    }

    @Override
    public QuizResponse getQuizById(UUID quizId, UUID orgId) {
        Quiz quiz = quizRepository.findByIdAndOrgId(quizId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + quizId));
        return mapToResponse(quiz);
    }

    @Override
    @Transactional
    public BulkInviteResponse bulkInviteCandidates(UUID quizId, BulkInviteRequest request, UUID orgId) {
        // Verify quiz ownership
        quizRepository.findByIdAndOrgId(quizId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + quizId));

        List<String> rawEmails = request.emails();
        int totalRequested = rawEmails.size();
        int skipped = 0;
        List<InvitationResponse> created = new ArrayList<>();

        for (String raw : rawEmails) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            String email = raw.trim().toLowerCase();

            Optional<QuizInvitation> existing = quizInvitationRepository.findByQuizIdAndCandidateEmail(quizId, email);
            if (existing.isPresent()) {
                skipped++;
            } else {
                String token = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
                QuizInvitation invitation = QuizInvitation.builder()
                        .orgId(orgId)
                        .quizId(quizId)
                        .candidateEmail(email)
                        .token(token)
                        .status("PENDING")
                        .invitedAt(LocalDateTime.now())
                        .build();

                QuizInvitation saved = quizInvitationRepository.save(invitation);
                created.add(mapToInvitationResponse(saved));
            }
        }

        return new BulkInviteResponse(totalRequested, created.size(), skipped, created);
    }

    @Override
    public List<InvitationResponse> getInvitationsByQuiz(UUID quizId, UUID orgId) {
        quizRepository.findByIdAndOrgId(quizId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + quizId));

        return quizInvitationRepository.findByOrgIdAndQuizId(orgId, quizId).stream()
                .map(this::mapToInvitationResponse)
                .toList();
    }

    @Override
    public InvitationResponse getInvitationByToken(String token) {
        QuizInvitation invitation = quizInvitationRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired invitation token"));
        return mapToInvitationResponse(invitation);
    }

    private QuizResponse mapToResponse(Quiz quiz) {
        return QuizResponse.builder()
                .id(quiz.getId())
                .orgId(quiz.getOrgId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .published(quiz.isPublished())
                .createdBy(quiz.getCreatedBy())
                .createdAt(quiz.getCreatedAt())
                .build();
    }

    private InvitationResponse mapToInvitationResponse(QuizInvitation i) {
        return new InvitationResponse(
                i.getId(),
                i.getQuizId(),
                i.getCandidateEmail(),
                i.getToken(),
                i.getStatus(),
                i.getInvitedAt()
        );
    }
}
