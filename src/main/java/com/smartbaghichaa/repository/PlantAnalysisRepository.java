package com.smartbaghichaa.repository;

import com.smartbaghichaa.entity.PlantAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PlantAnalysisRepository extends JpaRepository<PlantAnalysis, Long> {

    List<PlantAnalysis> findByUserEmailAndPlantNameOrderByAnalyzedAtDesc(String email, String plantName);

    List<PlantAnalysis> findByUserEmailAndPlantNameOrderByAnalyzedAtAsc(String email, String plantName);

    List<PlantAnalysis> findByUserEmailOrderByAnalyzedAtDesc(String email);

    // Rate-limit: count today's analyses per user
    long countByUserEmailAndAnalyzedAtAfter(String userEmail, LocalDateTime since);

    // Account deletion
    @Modifying
    @Transactional
    void deleteByUserEmail(String userEmail);
}
