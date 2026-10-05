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
@Table(name = "quiz")
public class QuizModel {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String title;

    private String description;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private UserModel createdBy;

    private boolean isActive;

    private int totalMarks;

    private LocalDateTime createdAt;

}
