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
@Table(name = "option")
public class OptionsModel {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private QuestionModel question;

    @Column(nullable = false)
    private String optionText;

    private boolean isCorrect;
}
