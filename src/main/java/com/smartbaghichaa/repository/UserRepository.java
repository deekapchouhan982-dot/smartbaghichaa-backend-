package com.smartbaghichaa.repository;

import com.smartbaghichaa.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    // ISSUE-06: Count distinct non-null cities for live stats
    @Query("SELECT COUNT(DISTINCT u.city) FROM User u WHERE u.city IS NOT NULL AND u.city != ''")
    long countDistinctCities();
}
