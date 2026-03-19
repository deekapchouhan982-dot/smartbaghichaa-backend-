package com.smartbaghichaa.repository;

import com.smartbaghichaa.entity.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    Optional<Reminder> findByUserId(Long userId);
}
