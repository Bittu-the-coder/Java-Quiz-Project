package com.bittuthecoder.quiz_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "quiz_invitation", uniqueConstraints = {
        @UniqueConstraint(name = "uq_quiz_candidate", columnNames = {"quiz_id", "candidate_email"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizInvitation {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "org_id", nullable = false)
    private UUID orgId;

    @Column(name = "quiz_id", nullable = false)
    private UUID quizId;

    @Column(name = "candidate_email", nullable = false)
    private String candidateEmail;

    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @Builder.Default
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @CreationTimestamp
    @Column(name = "invited_at")
    private LocalDateTime invitedAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;
}
