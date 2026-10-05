package com.bittuthecoder.quiz_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "quizzes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quiz {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "org_id", nullable = false)
    private UUID orgId;

    @Column(nullable = false)
    private String title;

    private String description;

    private boolean published;

    @Column(nullable = false)
    private String createdBy; // email from gateway

    @CreationTimestamp
    private LocalDateTime createdAt;
}
