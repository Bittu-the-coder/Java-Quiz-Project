package com.bittuthecoder.quiz_app.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "question")
public class QuestionModel {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private QuizModel quiz;

    @Column(nullable = false)
    private String questionText;

    private String questionType;

    private int marks;
}
