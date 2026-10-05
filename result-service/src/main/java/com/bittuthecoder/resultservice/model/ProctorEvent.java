package com.bittuthecoder.resultservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "proctor_event", uniqueConstraints = {
        @UniqueConstraint(name = "uq_attempt_sequence", columnNames = {"attempt_id", "sequence_num"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProctorEvent {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "org_id", nullable = false)
    private UUID orgId;

    @Column(name = "attempt_id", nullable = false)
    private UUID attemptId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private ProctorEventType eventType;

    @Column(name = "sequence_num", nullable = false)
    private int sequenceNum;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @CreationTimestamp
    @Column(name = "recorded_at")
    private LocalDateTime recordedAt;

    @Column(length = 128)
    private String signature;

    @Column(columnDefinition = "TEXT")
    private String details;
}
