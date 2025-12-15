package com.bittuthecoder.resultservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Answer {

    @Id
    @GeneratedValue
    private UUID id;

    private UUID questionId;

    private UUID selectedOptionId;

    private boolean correct;

    @ManyToOne
    @JoinColumn(name = "attempt_id")
    private QuizAttempt attempt;
}
