package com.smartbaghichaa.repository;

import com.smartbaghichaa.entity.PlantReminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PlantReminderRepository extends JpaRepository<PlantReminder, Long> {
    List<PlantReminder> findByUserId(Long userId);
    List<PlantReminder> findByUserPlantIdAndUserId(Long userPlantId, Long userId);
    void deleteByUserPlantId(Long userPlantId);
    List<PlantReminder> findByEnabledTrueAndNextReminderDateLessThanEqual(LocalDate date);
}
