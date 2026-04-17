package com.smartbaghichaa.controller;

import com.smartbaghichaa.service.ReminderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    @Autowired
    private ReminderService reminderService;

    // ── GET /api/reminders ──────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<?> getReminders(Principal principal) {
        try {
            return ResponseEntity.ok(reminderService.getReminders(principal.getName()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ── POST /api/reminders ─────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> saveReminders(Principal principal,
                                           @RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(reminderService.saveReminders(principal.getName(), body));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
