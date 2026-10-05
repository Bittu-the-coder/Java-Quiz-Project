package com.bittuthecoder.resultservice.dto;

import com.bittuthecoder.resultservice.model.ProctorEventType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ProctorEventRequest(
        @NotNull(message = "eventType is required")
        ProctorEventType eventType,

        @Min(value = 1, message = "sequenceNum must be >= 1")
        int sequenceNum,

        LocalDateTime occurredAt,

        String signature,

        String details
) {}
