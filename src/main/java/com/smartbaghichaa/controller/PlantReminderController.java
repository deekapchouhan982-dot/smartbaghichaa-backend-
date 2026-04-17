package com.smartbaghichaa.controller;

import com.smartbaghichaa.dto.PlantReminderRequest;
import com.smartbaghichaa.security.JwtUtil;
import com.smartbaghichaa.service.PlantReminderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * ISSUE-10: Plant-specific reminder CRUD endpoints.
 *
 * GET  /api/plant-reminders                    — all reminders for user
 * GET  /api/plant-reminders/plant/{plantId}    — reminders for one plant
 * POST /api/plant-reminders                    — create reminder
 * PUT  /api/plant-reminders/{id}               — update reminder
 * DELETE /api/plant-reminders/{id}             — delete reminder
 */
@RestController
@RequestMapping("/api/plant-reminders")
public class PlantReminderController {

    @Autowired private PlantReminderService service;
    @Autowired private JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<?> getAll(@RequestHeader("Authorization") String authHeader) {
        String email = extractEmail(authHeader);
        if (email == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        try {
            return ResponseEntity.ok(Map.of("reminders", service.getReminders(email)));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/plant/{plantId}")
    public ResponseEntity<?> getForPlant(@RequestHeader("Authorization") String authHeader,
                                          @PathVariable Long plantId) {
        String email = extractEmail(authHeader);
        if (email == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        try {
            return ResponseEntity.ok(Map.of("reminders", service.getRemindersForPlant(email, plantId)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestHeader("Authorization") String authHeader,
                                     @Valid @RequestBody PlantReminderRequest req) {
        String email = extractEmail(authHeader);
        if (email == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        try {
            return ResponseEntity.ok(service.createReminder(email, req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@RequestHeader("Authorization") String authHeader,
                                     @PathVariable Long id,
                                     @Valid @RequestBody PlantReminderRequest req) {
        String email = extractEmail(authHeader);
        if (email == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        try {
            return ResponseEntity.ok(service.updateReminder(email, id, req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@RequestHeader("Authorization") String authHeader,
                                     @PathVariable Long id) {
        String email = extractEmail(authHeader);
        if (email == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        try {
            service.deleteReminder(email, id);
            return ResponseEntity.ok(Map.of("deleted", true, "id", id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
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
