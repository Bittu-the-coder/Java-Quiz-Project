package com.bittuthecoder.resultservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "item_analysis", uniqueConstraints = {
        @UniqueConstraint(name = "uq_quiz_question_item", columnNames = {"quiz_id", "question_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemAnalysis {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "org_id", nullable = false)
    private UUID orgId;

    @Column(name = "quiz_id", nullable = false)
    private UUID quizId;

    @Column(name = "question_id", nullable = false)
    private UUID questionId;

    @Column(name = "total_attempts", nullable = false)
    private int totalAttempts;

    @Column(name = "correct_attempts", nullable = false)
    private int correctAttempts;

    @Column(name = "difficulty_index", nullable = false)
    private double difficultyIndex;

    @Column(name = "discrimination_idx", nullable = false)
    private double discriminationIndex;
}
