package com.bittuthecoder.question_service.repository;

import com.bittuthecoder.question_service.model.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface OptionRepository extends JpaRepository<Option, UUID> {

    boolean existsByIdAndQuestionIdAndCorrectTrue(
            UUID optionId,
            UUID questionId
    );

    @Query("SELECT COUNT(o) > 0 FROM Option o WHERE o.id = :optionId AND o.question.id = :questionId AND o.question.orgId = :orgId AND o.correct = true")
    boolean existsByIdAndQuestionIdAndOrgIdAndCorrectTrue(
            @Param("optionId") UUID optionId,
            @Param("questionId") UUID questionId,
            @Param("orgId") UUID orgId
    );
}
