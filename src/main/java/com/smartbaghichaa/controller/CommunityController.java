package com.smartbaghichaa.controller;

import com.smartbaghichaa.dto.CommentRequest;
import com.smartbaghichaa.dto.PostRequest;
import com.smartbaghichaa.security.JwtUtil;
import com.smartbaghichaa.service.CommunityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/community")
@CrossOrigin(origins = "*")
public class CommunityController {

    @Autowired private CommunityService communityService;
    @Autowired private JwtUtil jwtUtil;

    // ── GET /api/community/posts (public) ─────────────────────────────────
    @GetMapping("/posts")
    public ResponseEntity<?> getPosts(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        String viewerEmail = authHeader != null ? extractEmail(authHeader) : null;
        try {
            return ResponseEntity.ok(Map.of("posts", communityService.getAllPosts(viewerEmail)));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ── POST /api/community/posts (JWT) ───────────────────────────────────
    @PostMapping("/posts")
    public ResponseEntity<?> createPost(@RequestHeader("Authorization") String authHeader,
                                         @RequestBody PostRequest req) {
        String email = extractEmail(authHeader);
        if (email == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        try {
            return ResponseEntity.ok(communityService.createPost(email, req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ── POST /api/community/posts/{id}/like (JWT) ─────────────────────────
    @PostMapping("/posts/{id}/like")
    public ResponseEntity<?> toggleLike(@RequestHeader("Authorization") String authHeader,
                                         @PathVariable Long id) {
        String email = extractEmail(authHeader);
        if (email == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        try {
            return ResponseEntity.ok(communityService.toggleLike(email, id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ── POST /api/community/posts/{id}/dislike (JWT) ──────────────────────
    @PostMapping("/posts/{id}/dislike")
    public ResponseEntity<?> toggleDislike(@RequestHeader("Authorization") String authHeader,
                                            @PathVariable Long id) {
        String email = extractEmail(authHeader);
        if (email == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        try {
            return ResponseEntity.ok(communityService.toggleDislike(email, id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ── POST /api/community/posts/{id}/comments (JWT) ─────────────────────
    @PostMapping("/posts/{id}/comments")
    public ResponseEntity<?> addComment(@RequestHeader("Authorization") String authHeader,
                                         @PathVariable Long id,
                                         @RequestBody CommentRequest req) {
        String email = extractEmail(authHeader);
        if (email == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        try {
            return ResponseEntity.ok(communityService.addComment(email, id, req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ── GET /api/community/posts/{id}/comments ────────────────────────────
    @GetMapping("/posts/{id}/comments")
    public ResponseEntity<?> getComments(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(Map.of("comments", communityService.getComments(id)));
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
