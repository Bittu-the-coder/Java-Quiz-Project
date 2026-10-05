package com.bittuthecoder.quiz_service.repository;

import com.bittuthecoder.quiz_service.model.QuizInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuizInvitationRepository extends JpaRepository<QuizInvitation, UUID> {

    List<QuizInvitation> findByOrgIdAndQuizId(UUID orgId, UUID quizId);

    Optional<QuizInvitation> findByToken(String token);

    Optional<QuizInvitation> findByQuizIdAndCandidateEmail(UUID quizId, String candidateEmail);
}
