package com.bittuthecoder.resultservice.service.impl;

import com.bittuthecoder.common.error.BadRequestException;
import com.bittuthecoder.common.error.ConflictException;
import com.bittuthecoder.common.error.ResourceNotFoundException;
import com.bittuthecoder.resultservice.dto.ProctorAuditLogResponse;
import com.bittuthecoder.resultservice.dto.ProctorEventRequest;
import com.bittuthecoder.resultservice.dto.ProctorEventResponse;
import com.bittuthecoder.resultservice.model.AttemptStatus;
import com.bittuthecoder.resultservice.model.ProctorEvent;
import com.bittuthecoder.resultservice.model.QuizAttempt;
import com.bittuthecoder.resultservice.repository.ProctorEventRepository;
import com.bittuthecoder.resultservice.repository.QuizAttemptRepository;
import com.bittuthecoder.resultservice.service.ProctorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProctorServiceImpl implements ProctorService {

    private final QuizAttemptRepository attemptRepository;
    private final ProctorEventRepository proctorEventRepository;

    @Override
    @Transactional
    public ProctorEventResponse recordEvent(
            UUID attemptId,
            ProctorEventRequest request,
            UUID orgId,
            String studentEmail
    ) {
        QuizAttempt attempt = attemptRepository.findByIdAndOrgIdAndStudentEmail(attemptId, orgId, studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz attempt not found with id: " + attemptId));

        if (attempt.getStatus() == AttemptStatus.TERMINATED) {
            throw new ConflictException("Attempt has already been terminated");
        }

        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new ConflictException("Attempt is not in progress: " + attempt.getStatus());
        }

        // Strict monotonic sequence enforcement (anti-replay and anti-forgery)
        int maxSeq = proctorEventRepository.findMaxSequenceNumByAttemptId(attemptId);
        if (request.sequenceNum() <= maxSeq) {
            throw new BadRequestException("Replayed or duplicate proctor sequence number: "
                    + request.sequenceNum() + " (current max is " + maxSeq + ")");
        }
        if (request.sequenceNum() > maxSeq + 1) {
            throw new BadRequestException("Missing proctor sequence event. Expected "
                    + (maxSeq + 1) + " but received " + request.sequenceNum());
        }

        ProctorEvent event = ProctorEvent.builder()
                .orgId(orgId)
                .attemptId(attemptId)
                .eventType(request.eventType())
                .sequenceNum(request.sequenceNum())
                .occurredAt(request.occurredAt() != null ? request.occurredAt() : LocalDateTime.now())
                .signature(request.signature())
                .details(request.details())
                .build();

        ProctorEvent saved = proctorEventRepository.save(event);

        // Server-side violation policy: 3 violations -> WARNING, 5 violations -> TERMINATED
        long totalViolations = proctorEventRepository.countByAttemptId(attemptId);
        String actionTaken;

        if (totalViolations >= 5) {
            actionTaken = "TERMINATED";
            log.warn("Attempt {} reached 5 proctoring violations. TERMINATING attempt.", attemptId);
            attempt.setStatus(AttemptStatus.TERMINATED);
            attempt.setSubmittedAt(LocalDateTime.now());
            attemptRepository.save(attempt);
        } else if (totalViolations >= 3) {
            actionTaken = "WARNING";
            log.info("Attempt {} reached 3 proctoring violations. Issuing WARNING.", attemptId);
        } else {
            actionTaken = "RECORDED";
        }

        return new ProctorEventResponse(
                saved.getId(),
                saved.getAttemptId(),
                saved.getEventType(),
                saved.getSequenceNum(),
                saved.getRecordedAt(),
                actionTaken,
                attempt.getStatus()
        );
    }

    @Override
    public List<ProctorAuditLogResponse> getAuditTrail(UUID attemptId, UUID orgId) {
        attemptRepository.findByIdAndOrgId(attemptId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz attempt not found with id: " + attemptId));

        return proctorEventRepository.findByAttemptIdAndOrgIdOrderBySequenceNumAsc(attemptId, orgId).stream()
                .map(e -> new ProctorAuditLogResponse(
                        e.getId(),
                        e.getAttemptId(),
                        e.getEventType(),
                        e.getSequenceNum(),
                        e.getOccurredAt(),
                        e.getRecordedAt(),
                        e.getSignature(),
                        e.getDetails()
                ))
                .toList();
    }
}
