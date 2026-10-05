package com.bittuthecoder.quiz_service.dto;

import java.util.List;

public record BulkInviteResponse(
        int totalRequested,
        int successfullyInvited,
        int duplicatesSkipped,
        List<InvitationResponse> invitations
) {}
