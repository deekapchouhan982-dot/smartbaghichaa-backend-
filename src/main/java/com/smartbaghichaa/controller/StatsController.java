package com.smartbaghichaa.controller;

import com.smartbaghichaa.repository.GrowthLogRepository;
import com.smartbaghichaa.repository.PlantRepository;
import com.smartbaghichaa.repository.UserPlantRepository;
import com.smartbaghichaa.repository.UserRepository;
import com.smartbaghichaa.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired private UserRepository userRepository;
    @Autowired private PlantRepository plantRepository;
    @Autowired private UserPlantRepository userPlantRepository;
    @Autowired private GrowthLogRepository growthLogRepository;
    @Autowired private JwtUtil jwtUtil;

    // ISSUE-06: Return live counts instead of hardcoded values
    @GetMapping("/counts")
    public ResponseEntity<?> getCounts() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userCount",  userRepository.count());
        result.put("plantCount", plantRepository.count());
        result.put("cityCount",  userRepository.countDistinctCities());
        return ResponseEntity.ok(result);
    }

    // ISSUE-17 + M6: Per-user stats. required=false prevents Spring 400 when header is absent.
    @GetMapping("/me")
    public ResponseEntity<?> getMyStats(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        String token = authHeader.substring(7);
        if (!jwtUtil.isTokenValid(token))
            return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
        String email = jwtUtil.extractEmail(token);

        com.smartbaghichaa.entity.User user = userRepository.findByEmail(email)
            .orElse(null);
        if (user == null)
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));

        List<com.smartbaghichaa.entity.UserPlant> myPlants =
            userPlantRepository.findByUserId(user.getId());
        long myPlantCount = myPlants.size();

        List<Long> plantIds = myPlants.stream()
            .map(com.smartbaghichaa.entity.UserPlant::getId)
            .collect(java.util.stream.Collectors.toList());

        long totalLogs = plantIds.isEmpty() ? 0
            : growthLogRepository.findByUserPlantIdInOrderByLoggedAtDesc(plantIds).size();

        // Logs this week (last 7 days)
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        long logsThisWeek = plantIds.isEmpty() ? 0
            : growthLogRepository.findByUserPlantIdInOrderByLoggedAtDesc(plantIds).stream()
                .filter(l -> l.getLoggedAt() != null && l.getLoggedAt().isAfter(weekAgo))
                .count();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("myPlants",     myPlantCount);
        result.put("totalLogs",    totalLogs);
        result.put("logsThisWeek", logsThisWeek);
        return ResponseEntity.ok(result);
    }
}
