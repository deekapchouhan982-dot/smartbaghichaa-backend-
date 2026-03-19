package com.smartbaghichaa.controller;

import com.smartbaghichaa.dto.LogGrowthRequest;
import com.smartbaghichaa.security.JwtUtil;
import com.smartbaghichaa.service.TrackerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tracker")
@CrossOrigin(origins = "*")
public class TrackerController {

    @Autowired private TrackerService trackerService;
    @Autowired private JwtUtil jwtUtil;

    // ── POST /api/tracker/log ─────────────────────────────────────────────
    @PostMapping("/log")
    public ResponseEntity<?> logGrowth(@RequestHeader("Authorization") String authHeader,
                                        @RequestBody LogGrowthRequest req) {
        String email = extractEmail(authHeader);
        if (email == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        try {
            return ResponseEntity.ok(trackerService.logGrowth(email, req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ── GET /api/tracker/logs ─────────────────────────────────────────────
    @GetMapping("/logs")
    public ResponseEntity<?> getAllLogs(@RequestHeader("Authorization") String authHeader) {
        String email = extractEmail(authHeader);
        if (email == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        try {
            return ResponseEntity.ok(Map.of("logs", trackerService.getAllLogs(email)));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ── GET /api/tracker/logs/{plantId} ───────────────────────────────────
    @GetMapping("/logs/{plantId}")
    public ResponseEntity<?> getLogsForPlant(@RequestHeader("Authorization") String authHeader,
                                              @PathVariable Long plantId) {
        String email = extractEmail(authHeader);
        if (email == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        try {
            return ResponseEntity.ok(Map.of("logs", trackerService.getLogsForPlant(email, plantId)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ── GET /api/tracker/streak ───────────────────────────────────────────
    @GetMapping("/streak")
    public ResponseEntity<?> getStreak(@RequestHeader("Authorization") String authHeader) {
        String email = extractEmail(authHeader);
        if (email == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        try {
            return ResponseEntity.ok(trackerService.getStreak(email));
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
