package com.smartbaghichaa.repository;

import com.smartbaghichaa.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByEmail(String email);

    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiresAt < :now")
    void deleteAllExpired(@Param("now") LocalDateTime now);
}
