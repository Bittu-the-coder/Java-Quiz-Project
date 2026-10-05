package com.bittuthecoder.resultservice.repository;

import com.bittuthecoder.resultservice.model.ProctorEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProctorEventRepository extends JpaRepository<ProctorEvent, UUID> {

    List<ProctorEvent> findByAttemptIdAndOrgIdOrderBySequenceNumAsc(UUID attemptId, UUID orgId);

    @Query("SELECT COALESCE(MAX(e.sequenceNum), 0) FROM ProctorEvent e WHERE e.attemptId = :attemptId")
    int findMaxSequenceNumByAttemptId(@Param("attemptId") UUID attemptId);

    long countByAttemptId(UUID attemptId);
}
