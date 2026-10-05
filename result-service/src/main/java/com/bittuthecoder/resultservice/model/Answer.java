package com.bittuthecoder.resultservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "answer", uniqueConstraints = {
    @UniqueConstraint(name = "uq_attempt_question", columnNames = {"attempt_id", "question_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Answer {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "question_id", nullable = false)
    private UUID questionId;

    @Column(name = "selected_option_id")
    private UUID selectedOptionId;

    @Column(nullable = false)
    private boolean correct;

    @Column(name = "answered_at")
    private java.time.LocalDateTime answeredAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private QuizAttempt attempt;
}
