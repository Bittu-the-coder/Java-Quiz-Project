package com.bittuthecoder.question_service.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "question")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "org_id", nullable = false)
    private UUID orgId;

    @Column(nullable = false)
    private UUID quizId;   // belongs to Quiz Service

    @Column(nullable = false)
    private String text;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Difficulty difficulty = Difficulty.MEDIUM;

    @Column(length = 100)
    private String category;

    @Column(length = 255)
    private String tags;

    @Builder.Default
    @Column(nullable = false)
    private int marks = 1;

    @Builder.Default
    @Column(name = "negative_marks", nullable = false)
    private double negativeMarks = 0.00;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Option> options;
}
