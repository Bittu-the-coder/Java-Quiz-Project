package com.bittuthecoder.resultservice.service;

import com.bittuthecoder.resultservice.dto.ProctorAuditLogResponse;
import com.bittuthecoder.resultservice.dto.ProctorEventRequest;
import com.bittuthecoder.resultservice.dto.ProctorEventResponse;

import java.util.List;
import java.util.UUID;

public interface ProctorService {

    ProctorEventResponse recordEvent(UUID attemptId, ProctorEventRequest request, UUID orgId, String studentEmail);

    List<ProctorAuditLogResponse> getAuditTrail(UUID attemptId, UUID orgId);
}
