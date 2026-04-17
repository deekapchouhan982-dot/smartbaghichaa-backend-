package com.smartbaghichaa.repository;

import com.smartbaghichaa.entity.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {}
