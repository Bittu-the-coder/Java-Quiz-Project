package com.bittuthecoder.resultservice.dto;

import com.bittuthecoder.resultservice.model.AttemptStatus;
import com.bittuthecoder.resultservice.model.ProctorEventType;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProctorEventResponse(
        UUID eventId,
        UUID attemptId,
        ProctorEventType eventType,
        int sequenceNum,
        LocalDateTime recordedAt,
        String actionTaken,
        AttemptStatus attemptStatus
) {}
