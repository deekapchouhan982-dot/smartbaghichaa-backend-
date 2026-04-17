package com.smartbaghichaa.repository;

import com.smartbaghichaa.entity.EmailOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailOtpRepository extends JpaRepository<EmailOtp, Long> {
    Optional<EmailOtp> findTopByEmailAndTypeOrderByIdDesc(String email, String type);

    long countByEmailAndTypeAndCreatedAtAfter(String email, String type, LocalDateTime since);

    @Transactional
    void deleteByEmailAndType(String email, String type);

    @Modifying
    @Transactional
    @Query("DELETE FROM EmailOtp e WHERE e.email = :email AND e.type = :type AND (e.createdAt IS NULL OR e.createdAt < :before)")
    void deleteOldEntries(@Param("email") String email, @Param("type") String type, @Param("before") LocalDateTime before);

    // Account deletion: remove all OTPs for an email
    @Transactional
    void deleteByEmail(String email);

    // Nightly cleanup: remove all expired OTPs
    @Modifying
    @Transactional
    @Query("DELETE FROM EmailOtp e WHERE e.expiresAt < :now")
    void deleteAllExpired(@Param("now") LocalDateTime now);
}
