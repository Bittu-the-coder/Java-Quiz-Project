package com.bittuthecoder.resultservice.repository;

import com.bittuthecoder.resultservice.model.ItemAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ItemAnalysisRepository extends JpaRepository<ItemAnalysis, UUID> {

    List<ItemAnalysis> findByOrgIdAndQuizId(UUID orgId, UUID quizId);

    Optional<ItemAnalysis> findByQuizIdAndQuestionId(UUID quizId, UUID questionId);
}
