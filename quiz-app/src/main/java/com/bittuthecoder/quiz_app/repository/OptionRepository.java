package com.bittuthecoder.quiz_app.repository;

import com.bittuthecoder.quiz_app.models.OptionsModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;
public interface OptionRepository extends JpaRepository<OptionsModel, UUID> {
    List<OptionsModel> findByQuestionId(UUID questionId);
}