package com.smartbaghichaa.controller;

import com.smartbaghichaa.dto.CommentRequest;
import com.smartbaghichaa.dto.PostRequest;
import com.smartbaghichaa.security.JwtUtil;
import com.smartbaghichaa.service.CommunityService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/community")
public class CommunityController {

    @Autowired private CommunityService communityService;
    @Autowired private JwtUtil jwtUtil;

    // ── GET /api/community/posts?page=0&size=10 (public) ─────────────────
    @GetMapping("/posts")
    public ResponseEntity<?> getPosts(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        if (page < 0) page = 0;
        if (size < 1 || size > 50) size = 10;
        String viewerEmail = authHeader != null ? extractEmail(authHeader) : null;
        try {
            return ResponseEntity.ok(communityService.getPostsPaged(viewerEmail, page, size));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ── DELETE /api/community/posts/{id} (JWT, own posts only) ───────────
    @DeleteMapping("/posts/{id}")
    public ResponseEntity<?> deletePost(@RequestHeader("Authorization") String authHeader,
                                         @PathVariable Long id) {
        String email = extractEmail(authHeader);
        if (email == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        try {
            communityService.deletePost(email, id);
            return ResponseEntity.ok(Map.of("message", "Post deleted"));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ── POST /api/community/posts (JWT) ───────────────────────────────────
    @PostMapping("/posts")
    public ResponseEntity<?> createPost(@RequestHeader("Authorization") String authHeader,
                                         @Valid @RequestBody PostRequest req) {
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
                                         @Valid @RequestBody CommentRequest req) {
        String email = extractEmail(authHeader);
        if (email == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        try {
            return ResponseEntity.ok(communityService.addComment(email, id, req));
        } catch (java.util.NoSuchElementException e) {
            // H3: post not found → 404
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ── GET /api/community/posts/{id} (M11) ──────────────────────────────
    @GetMapping("/posts/{id}")
    public ResponseEntity<?> getPost(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        String viewerEmail = authHeader != null ? extractEmail(authHeader) : null;
        try {
            return ResponseEntity.ok(communityService.getPost(id, viewerEmail));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
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

    // ── DELETE /api/community/comments/{id} (M9, JWT) ────────────────────
    @DeleteMapping("/comments/{id}")
    public ResponseEntity<?> deleteComment(@RequestHeader("Authorization") String authHeader,
                                            @PathVariable Long id) {
        String email = extractEmail(authHeader);
        if (email == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        try {
            communityService.deleteComment(email, id);
            return ResponseEntity.ok(Map.of("deleted", true, "id", id));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
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
