package com.smartbaghichaa.repository;

import com.smartbaghichaa.entity.UserPlant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPlantRepository extends JpaRepository<UserPlant, Long> {
    List<UserPlant> findByUserId(Long userId);
    boolean existsByUserIdAndPlantName(Long userId, String plantName);
    Optional<UserPlant> findByIdAndUserId(Long id, Long userId);
}
