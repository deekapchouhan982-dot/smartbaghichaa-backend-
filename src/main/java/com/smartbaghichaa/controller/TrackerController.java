package com.smartbaghichaa.controller;

import com.smartbaghichaa.dto.LogGrowthRequest;
import com.smartbaghichaa.security.JwtUtil;
import com.smartbaghichaa.service.TrackerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.Map;

@RestController
@RequestMapping("/api/tracker")
public class TrackerController {

    @Autowired private TrackerService trackerService;
    @Autowired private JwtUtil jwtUtil;

    // ── POST /api/tracker/log ─────────────────────────────────────────────
    @PostMapping("/log")
    public ResponseEntity<?> logGrowth(@RequestHeader("Authorization") String authHeader,
                                        @Valid @RequestBody LogGrowthRequest req) {
        String email = extractEmail(authHeader);
        if (email == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        try {
            return ResponseEntity.ok(trackerService.logGrowth(email, req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ── GET /api/tracker/logs ─────────────────────────────────────────────
    // Supports ?page=0&size=20 (page is 0-indexed). Falls back to ?limit=N for backwards compat.
    @GetMapping("/logs")
    public ResponseEntity<?> getAllLogs(@RequestHeader("Authorization") String authHeader,
                                        @RequestParam(defaultValue = "0")  int page,
                                        @RequestParam(defaultValue = "20") int size,
                                        @RequestParam(defaultValue = "-1") int limit) {
        String email = extractEmail(authHeader);
        if (email == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        try {
            int effectiveSize = limit > 0 ? Math.min(limit, 200) : Math.min(size, 100);
            if (limit > 0) {
                // Legacy ?limit= path: pre-limit in service, no pagination
                java.util.List<?> logs = trackerService.getAllLogs(email, effectiveSize);
                return ResponseEntity.ok(java.util.Map.of(
                    "logs",    logs,
                    "page",    0,
                    "size",    effectiveSize,
                    "total",   logs.size(),
                    "hasMore", false
                ));
            }
            // Pagination path: fetch ALL logs, then slice the requested page
            java.util.List<?> allLogs = trackerService.getAllLogs(email, 0);
            int total = allLogs.size();
            int from  = page * effectiveSize;
            java.util.List<?> paged = from >= total ? java.util.Collections.emptyList()
                                                    : allLogs.subList(from, Math.min(from + effectiveSize, total));
            return ResponseEntity.ok(java.util.Map.of(
                "logs",     paged,
                "page",     page,
                "size",     effectiveSize,
                "total",    total,
                "hasMore",  (from + effectiveSize) < total
            ));
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

    // ── DELETE /api/tracker/log/{id} ─────────────────────────────────────
    @DeleteMapping("/log/{id}")
    public ResponseEntity<?> deleteLog(@RequestHeader("Authorization") String authHeader,
                                       @PathVariable Long id) {
        String email = extractEmail(authHeader);
        if (email == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        try {
            trackerService.deleteLog(email, id);
            return ResponseEntity.ok(Map.of("deleted", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ── PUT /api/tracker/log/{id} ────────────────────────────────────────
    @PutMapping("/log/{id}")
    public ResponseEntity<?> editLog(@RequestHeader("Authorization") String authHeader,
                                     @PathVariable Long id,
                                     @Valid @RequestBody LogGrowthRequest req) {
        String email = extractEmail(authHeader);
        if (email == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        try {
            return ResponseEntity.ok(trackerService.editLog(email, id, req));
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
