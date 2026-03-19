package com.smartbaghichaa.controller;

import com.smartbaghichaa.dto.RecommendRequest;
import com.smartbaghichaa.service.PlantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plants")
@CrossOrigin(origins = "*")
public class PlantController {

    @Autowired
    private PlantService plantService;

    // ── GET /api/plants[?search=term] ─────────────────────────────────────
    @GetMapping
    public ResponseEntity<?> getAllPlants(
            @RequestParam(required = false) String search) {
        try {
            List<Map<String, Object>> plants = (search != null && !search.isBlank())
                ? plantService.search(search)
                : plantService.getAllPlants();
            return ResponseEntity.ok(Map.of("plants", plants, "total", plants.size()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ── POST /api/plants/recommend ─────────────────────────────────────────
    @PostMapping("/recommend")
    public ResponseEntity<?> recommend(@RequestBody RecommendRequest req) {
        try {
            if (req.getCity() == null || req.getCity().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "City is required"));
            }
            if (req.getSeason() == null || req.getSeason().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Season is required"));
            }
            if (req.getSpace() == null || req.getSpace().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Space is required"));
            }
            List<Map<String, Object>> results = plantService.recommend(req);
            return ResponseEntity.ok(Map.of("plants", results, "total", results.size()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
