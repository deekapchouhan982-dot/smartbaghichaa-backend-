package com.smartbaghichaa.controller;

import com.smartbaghichaa.dto.AddPlantRequest;
import com.smartbaghichaa.security.JwtUtil;
import com.smartbaghichaa.service.GardenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/garden")
public class GardenController {

    @Autowired
    private GardenService gardenService;

    @Autowired
    private JwtUtil jwtUtil;

    // ── GET /api/garden ────────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<?> getGarden(@RequestHeader("Authorization") String authHeader) {
        String email = extractEmail(authHeader);
        if (email == null) return ResponseEntity.status(401).body(Map.of("error", "Invalid or missing token"));
        try {
            return ResponseEntity.ok(Map.of("plants", gardenService.getGarden(email)));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ── POST /api/garden/add ──────────────────────────────────────────────
    @PostMapping("/add")
    public ResponseEntity<?> addPlant(@RequestHeader("Authorization") String authHeader,
                                       @Valid @RequestBody AddPlantRequest req) {
        String email = extractEmail(authHeader);
        if (email == null) return ResponseEntity.status(401).body(Map.of("error", "Invalid or missing token"));
        try {
            return ResponseEntity.ok(gardenService.addPlant(email, req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ── PUT /api/garden/{id} (ISSUE-14) ──────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePlant(@RequestHeader("Authorization") String authHeader,
                                          @PathVariable Long id,
                                          @Valid @RequestBody AddPlantRequest req) {
        String email = extractEmail(authHeader);
        if (email == null) return ResponseEntity.status(401).body(Map.of("error", "Invalid or missing token"));
        try {
            return ResponseEntity.ok(gardenService.updatePlant(email, id, req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ── DELETE /api/garden/{id} ───────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePlant(@RequestHeader("Authorization") String authHeader,
                                          @PathVariable Long id) {
        String email = extractEmail(authHeader);
        if (email == null) return ResponseEntity.status(401).body(Map.of("error", "Invalid or missing token"));
        try {
            return ResponseEntity.ok(gardenService.deletePlant(email, id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    private String extractEmail(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        String token = authHeader.substring(7);
        if (!jwtUtil.isTokenValid(token)) return null;
        return jwtUtil.extractEmail(token);
    }
}
