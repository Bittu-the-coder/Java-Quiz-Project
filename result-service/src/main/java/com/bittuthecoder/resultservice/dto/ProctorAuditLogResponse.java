package com.bittuthecoder.resultservice.dto;

import com.bittuthecoder.resultservice.model.ProctorEventType;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProctorAuditLogResponse(
        UUID id,
        UUID attemptId,
        ProctorEventType eventType,
        int sequenceNum,
        LocalDateTime occurredAt,
        LocalDateTime recordedAt,
        String signature,
        String details
) {}
