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
@Table(name = "quiz_attempt_answer")
public class QuizAttemptAnswerModel {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "quiz_attempt_id")
    private QuizAttemptModel quizAttempt;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private QuestionModel question;

    @ManyToOne
    @JoinColumn(name = "selected_option_id")
    private OptionsModel selectedOption;

    private boolean isCorrect;
}

