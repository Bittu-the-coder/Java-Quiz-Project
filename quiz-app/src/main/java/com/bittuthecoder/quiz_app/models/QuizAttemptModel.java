package com.bittuthecoder.quiz_app.models;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "quiz_attempt")
public class QuizAttemptModel {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "quiz_id")
    private QuizModel quiz;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private UserModel student;

    private int score;

    private String status;

    private LocalDateTime startedAt;

    private LocalDateTime submittedAt;
}
