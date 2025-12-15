package com.bittuthecoder.question_service.repository;

import com.bittuthecoder.question_service.model.Option;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OptionRepository extends JpaRepository<Option, UUID> {

    boolean existsByIdAndQuestionIdAndCorrectTrue(
            UUID optionId,
            UUID questionId
    );
}
