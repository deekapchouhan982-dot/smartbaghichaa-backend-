package com.smartbaghichaa.repository;

import com.smartbaghichaa.entity.Plant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlantRepository extends JpaRepository<Plant, Long> {
    Optional<Plant> findByName(String name);
    Optional<Plant> findByNameIgnoreCase(String name);
    boolean existsByName(String name);

    @Query("SELECT p FROM Plant p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%',:term,'%')) OR " +
           "LOWER(p.category) LIKE LOWER(CONCAT('%',:term,'%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%',:term,'%'))")
    List<Plant> searchByTerm(@Param("term") String term);
}
