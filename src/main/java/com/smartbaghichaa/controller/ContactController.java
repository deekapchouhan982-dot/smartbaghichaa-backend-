package com.smartbaghichaa.controller;

import com.smartbaghichaa.dto.ContactMessageDto;
import com.smartbaghichaa.entity.ContactMessage;
import com.smartbaghichaa.repository.ContactMessageRepository;
import com.smartbaghichaa.security.JwtUtil;
import com.smartbaghichaa.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/contact")
public class ContactController {

    @Autowired private ContactMessageRepository contactRepo;
    @Autowired private EmailService emailService;
    @Autowired private JwtUtil jwtUtil;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    // ISSUE-07: In-memory IP-based rate limiting (60-second cooldown per IP)
    private final ConcurrentHashMap<String, LocalDateTime> lastContactTime = new ConcurrentHashMap<>();
    private static final int RATE_LIMIT_SECONDS = 60;

    // ── POST /api/contact (public) ─────────────────────────────
    @PostMapping
    public ResponseEntity<?> submit(@RequestBody ContactMessageDto req,
                                    HttpServletRequest httpRequest) {
        // ISSUE-07: Rate limit by IP — max 1 message per 60 seconds
        String ip = getClientIp(httpRequest);
        LocalDateTime lastTime = lastContactTime.get(ip);
        if (lastTime != null && lastTime.plusSeconds(RATE_LIMIT_SECONDS).isAfter(LocalDateTime.now())) {
            return ResponseEntity.status(429).body(Map.of("error",
                "Please wait " + RATE_LIMIT_SECONDS + " seconds before sending another message."));
        }

        String name    = req.getName()    != null ? req.getName().trim()    : "";
        String email   = req.getEmail()   != null ? req.getEmail().trim()   : "";
        String subject = req.getSubject() != null ? req.getSubject().trim() : "General Enquiry";
        String message = req.getMessage() != null ? req.getMessage().trim() : "";

        if (name.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("error", "Name is required"));
        if (email.isEmpty() || !email.contains("@"))
            return ResponseEntity.badRequest().body(Map.of("error", "Valid email is required"));
        if (message.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("error", "Message is required"));
        if (message.length() > 2000)
            return ResponseEntity.badRequest().body(Map.of("error", "Message too long (max 2000 chars)"));

        ContactMessage cm = new ContactMessage();
        cm.setName(name);
        cm.setEmail(email);
        cm.setSubject(subject);
        cm.setMessage(message);
        contactRepo.save(cm);

        // Record time for rate limiting after successful validation
        lastContactTime.put(ip, LocalDateTime.now());

        emailService.sendContactNotification(name, email, subject, message);

        return ResponseEntity.ok(Map.of("message", "Message received! We'll get back to you soon."));
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static final String ADMIN_EMAIL = System.getenv().getOrDefault("ADMIN_EMAIL", "tanujawala@gmail.com");

    // ── GET /api/contact/admin (admin-only) ────────────────────
    @GetMapping("/admin")
    public ResponseEntity<?> getAll(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        String token = authHeader.substring(7);
        if (!jwtUtil.isTokenValid(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
        }
        String callerEmail = jwtUtil.extractEmail(token);
        if (!ADMIN_EMAIL.equalsIgnoreCase(callerEmail)) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden — admin only"));
        }
        List<ContactMessage> messages = contactRepo.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Map<String, Object>> result = new ArrayList<>();
        for (ContactMessage m : messages) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("id",        m.getId());
            entry.put("name",      m.getName());
            entry.put("email",     m.getEmail());
            entry.put("subject",   m.getSubject());
            entry.put("message",   m.getMessage());
            entry.put("createdAt", m.getCreatedAt() != null ? m.getCreatedAt().format(FMT) : "");
            result.add(entry);
        }
        return ResponseEntity.ok(Map.of("total", result.size(), "messages", result));
    }
}
