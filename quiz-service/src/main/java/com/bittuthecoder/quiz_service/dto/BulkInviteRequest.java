package com.bittuthecoder.quiz_service.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BulkInviteRequest(
        @NotEmpty(message = "Candidate emails list cannot be empty")
        List<String> emails
) {}
