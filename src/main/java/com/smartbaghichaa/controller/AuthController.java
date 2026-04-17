package com.smartbaghichaa.controller;

import com.smartbaghichaa.dto.*;
import com.smartbaghichaa.security.JwtUtil;
import com.smartbaghichaa.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private AuthService authService;
    @Autowired private JwtUtil jwtUtil;

    // ─── POST /api/auth/send-signup-otp ───────────────────────
    @PostMapping("/send-signup-otp")
    public ResponseEntity<?> sendSignupOtp(@RequestBody Map<String, String> body) {
        try {
            String email = body.getOrDefault("email", "");
            String name  = body.getOrDefault("name",  "");
            String msg   = authService.sendSignupOtp(email, name);
            return ResponseEntity.ok(Map.of("message", msg));
        } catch (IllegalStateException e) {
            int status = e.getMessage().contains("Too many") ? 429 : 400;
            return ResponseEntity.status(status).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── POST /api/auth/signup ─────────────────────────────────
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest req) {
        try {
            return ResponseEntity.ok(authService.signup(req));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── POST /api/auth/login ──────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        try {
            return ResponseEntity.ok(authService.login(req));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(429).body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    // ─── POST /api/auth/forgot-password ───────────────────────
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        try {
            String message = authService.forgotPassword(req.getEmail());
            return ResponseEntity.ok(Map.of("message", message));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to send email: " + e.getMessage()));
        }
    }

    // ─── POST /api/auth/send-forgot-otp ───────────────────────
    @PostMapping("/send-forgot-otp")
    public ResponseEntity<?> sendForgotOtp(@RequestBody Map<String, String> body) {
        try {
            String msg = authService.sendForgotOtp(body.getOrDefault("email", ""));
            return ResponseEntity.ok(Map.of("message", msg));
        } catch (IllegalStateException e) {
            int status = e.getMessage().contains("Too many") ? 429 : 400;
            return ResponseEntity.status(status).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to send OTP: " + e.getMessage()));
        }
    }

    // ─── POST /api/auth/reset-password ────────────────────────
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        try {
            String msg = authService.resetPasswordWithOtp(req);
            return ResponseEntity.ok(Map.of("message", msg));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Reset failed: " + e.getMessage()));
        }
    }

    // ─── PUT /api/auth/profile ─────────────────────────────────
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UpdateProfileRequest req) {
        try {
            String email = extractEmail(authHeader);
            if (email == null) return ResponseEntity.status(401).body(Map.of("error", "Invalid or missing token"));
            return ResponseEntity.ok(authService.updateProfile(email, req));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── GET /api/auth/me ──────────────────────────────────────
    @GetMapping("/me")
    public ResponseEntity<?> getMe(@RequestHeader("Authorization") String authHeader) {
        try {
            String email = extractEmail(authHeader);
            if (email == null) return ResponseEntity.status(401).body(Map.of("error", "Invalid or missing token"));
            AuthResponse resp = authService.getMe(email);
            // M10: return Map without the token field (token:null pollutes /me response)
            Map<String, Object> body = new HashMap<>();
            body.put("name",     resp.getName());
            body.put("email",    resp.getEmail());
            body.put("city",     resp.getCity());
            body.put("joinedAt", resp.getJoinedAt());
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    // ─── DELETE /api/auth/account ──────────────────────────────
    @DeleteMapping("/account")
    public ResponseEntity<?> deleteAccount(@RequestHeader("Authorization") String authHeader) {
        try {
            String email = extractEmail(authHeader);
            if (email == null) return ResponseEntity.status(401).body(Map.of("error", "Invalid or missing token"));
            authService.deleteAccount(email);
            return ResponseEntity.ok(Map.of("message", "Account deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Deletion failed: " + e.getMessage()));
        }
    }

    private String extractEmail(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        String token = authHeader.substring(7);
        if (!jwtUtil.isTokenValid(token)) return null;
        return jwtUtil.extractEmail(token);
    }
}
