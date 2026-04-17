package com.smartbaghichaa.repository;

import com.smartbaghichaa.entity.GrowthLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface GrowthLogRepository extends JpaRepository<GrowthLog, Long> {

    List<GrowthLog> findByUserPlantIdOrderByLoggedAtDesc(Long userPlantId);

    @Query("SELECT g FROM GrowthLog g WHERE g.userPlantId IN :ids ORDER BY g.loggedAt DESC")
    List<GrowthLog> findByUserPlantIdInOrderByLoggedAtDesc(@Param("ids") List<Long> ids);

    // Account deletion
    @Modifying
    @Transactional
    void deleteByUserPlantIdIn(java.util.List<Long> ids);
}
