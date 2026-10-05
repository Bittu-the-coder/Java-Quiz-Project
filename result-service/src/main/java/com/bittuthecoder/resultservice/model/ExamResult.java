package com.bittuthecoder.resultservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "exam_result")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamResult {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "org_id", nullable = false)
    private UUID orgId;

    @Column(name = "quiz_id", nullable = false)
    private UUID quizId;

    @Column(name = "attempt_id", nullable = false, unique = true)
    private UUID attemptId;

    @Column(name = "student_email", nullable = false)
    private String studentEmail;

    @Column(name = "total_score", nullable = false)
    private double totalScore;

    @Column(name = "max_possible_score", nullable = false)
    private double maxPossibleScore;

    @Column(nullable = false)
    private double percentage;

    @Column(name = "rank_in_exam")
    private Integer rankInExam;

    private Double percentile;

    @Column(nullable = false)
    private boolean passed;

    @CreationTimestamp
    @Column(name = "graded_at")
    private LocalDateTime gradedAt;
}
