package com.smartbaghichaa.service;

import com.smartbaghichaa.dto.CommentRequest;
import com.smartbaghichaa.dto.PostRequest;
import com.smartbaghichaa.entity.CommunityPost;
import com.smartbaghichaa.entity.PostComment;
import com.smartbaghichaa.entity.PostLike;
import com.smartbaghichaa.entity.User;
import com.smartbaghichaa.repository.CommunityPostRepository;
import com.smartbaghichaa.repository.PostCommentRepository;
import com.smartbaghichaa.repository.PostLikeRepository;
import com.smartbaghichaa.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommunityService {

    @Autowired private CommunityPostRepository postRepository;
    @Autowired private PostCommentRepository commentRepository;
    @Autowired private PostLikeRepository likeRepository;
    @Autowired private UserRepository userRepository;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    // ── GET ALL POSTS ─────────────────────────────────────────────────────
    public List<Map<String, Object>> getAllPosts(String viewerEmail) {
        List<CommunityPost> posts = postRepository.findAllByOrderByCreatedAtDesc();
        return posts.stream().map(p -> toPostMap(p, viewerEmail)).collect(Collectors.toList());
    }

    // ── CREATE POST ───────────────────────────────────────────────────────
    public Map<String, Object> createPost(String email, PostRequest req) {
        if (req.getContent() == null || req.getContent().isBlank())
            throw new IllegalArgumentException("Content cannot be empty");
        if (req.getContent().length() > 5000)
            throw new IllegalArgumentException("Content must be 5000 characters or less");

        // Auto-derive title from content if not provided (Twitter-style posts have no title)
        String title = (req.getTitle() != null && !req.getTitle().isBlank())
            ? req.getTitle()
            : (req.getContent().length() > 80 ? req.getContent().substring(0, 80) + "…" : req.getContent());

        User user = findUser(email);
        String tagsStr = req.getTags() != null ? String.join(",", req.getTags()) : "";

        CommunityPost post = new CommunityPost();
        post.setAuthorEmail(email);
        post.setAuthorName(user.getName());
        post.setTitle(escapeHtml(title));   // auto-derived or user-provided
        post.setContent(escapeHtml(req.getContent()));
        post.setTags(escapeHtml(tagsStr));
        if (req.getPhotoData() != null && !req.getPhotoData().isBlank())
            post.setPhotoData(req.getPhotoData());

        CommunityPost saved = postRepository.save(post);
        return toPostMap(saved, email);
    }

    // ── DELETE POST (author only) ─────────────────────────────────────────
    @Transactional
    public void deletePost(String email, Long postId) {
        CommunityPost post = findPost(postId);
        if (!post.getAuthorEmail().equals(email))
            throw new SecurityException("You can only delete your own posts");
        commentRepository.deleteByPostId(postId);
        likeRepository.deleteByPostId(postId);
        postRepository.delete(post);
    }

    // ── GET POSTS (paged) ─────────────────────────────────────────────────
    public Map<String, Object> getPostsPaged(String viewerEmail, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), 20); // 1–20 posts per page
        int offset   = Math.max(page, 0) * safeSize;
        List<CommunityPost> all = postRepository.findAllByOrderByCreatedAtDesc();
        int total = all.size();
        List<Map<String, Object>> items = all.stream()
            .skip(offset).limit(safeSize)
            .map(p -> toPostMap(p, viewerEmail))
            .collect(Collectors.toList());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("posts",    items);
        result.put("total",    total);
        result.put("page",     page);
        result.put("size",     safeSize);
        result.put("hasMore",  offset + safeSize < total);
        return result;
    }

    // ── TOGGLE LIKE ───────────────────────────────────────────────────────
    public Map<String, Object> toggleLike(String email, Long postId) {
        CommunityPost post = findPost(postId);
        Optional<PostLike> existing = likeRepository.findByPostIdAndUserEmail(postId, email);

        if (existing.isPresent()) {
            PostLike pl = existing.get();
            if ("LIKE".equals(pl.getReaction())) {
                // Remove like
                likeRepository.delete(pl);
                post.setLikes(Math.max(0, post.getLikes() - 1));
            } else {
                // Switch from DISLIKE to LIKE
                post.setDislikes(Math.max(0, post.getDislikes() - 1));
                post.setLikes(post.getLikes() + 1);
                pl.setReaction("LIKE");
                likeRepository.save(pl);
            }
        } else {
            post.setLikes(post.getLikes() + 1);
            likeRepository.save(new PostLike(postId, email, "LIKE"));
        }

        postRepository.save(post);
        return toPostMap(post, email);
    }

    // ── TOGGLE DISLIKE ────────────────────────────────────────────────────
    public Map<String, Object> toggleDislike(String email, Long postId) {
        CommunityPost post = findPost(postId);
        Optional<PostLike> existing = likeRepository.findByPostIdAndUserEmail(postId, email);

        if (existing.isPresent()) {
            PostLike pl = existing.get();
            if ("DISLIKE".equals(pl.getReaction())) {
                // Remove dislike
                likeRepository.delete(pl);
                post.setDislikes(Math.max(0, post.getDislikes() - 1));
            } else {
                // Switch from LIKE to DISLIKE
                post.setLikes(Math.max(0, post.getLikes() - 1));
                post.setDislikes(post.getDislikes() + 1);
                pl.setReaction("DISLIKE");
                likeRepository.save(pl);
            }
        } else {
            post.setDislikes(post.getDislikes() + 1);
            likeRepository.save(new PostLike(postId, email, "DISLIKE"));
        }

        postRepository.save(post);
        return toPostMap(post, email);
    }

    // ── ADD COMMENT ───────────────────────────────────────────────────────
    public Map<String, Object> addComment(String email, Long postId, CommentRequest req) {
        // H3: post-not-found must be 404, not 400 — use NoSuchElementException
        postRepository.findById(postId)
            .orElseThrow(() -> new java.util.NoSuchElementException("Post not found"));
        User user = findUser(email);

        // ISSUE-16: Accept both 'text' (new) and 'content' (legacy) field names
        String commentText = req.getEffectiveContent();
        if (commentText == null || commentText.isBlank())
            throw new IllegalArgumentException("Comment cannot be empty");
        if (commentText.length() > 300)
            throw new IllegalArgumentException("Comment must be 300 characters or less");

        PostComment comment = new PostComment();
        comment.setPostId(postId);
        comment.setAuthorName(user.getName());
        comment.setAuthorEmail(email);
        comment.setContent(escapeHtml(commentText));

        PostComment saved = commentRepository.save(comment);
        return toCommentMap(saved);
    }

    // ── GET COMMENTS ──────────────────────────────────────────────────────
    public List<Map<String, Object>> getComments(Long postId) {
        findPost(postId);
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId)
            .stream().map(this::toCommentMap).collect(Collectors.toList());
    }

    // ── GET SINGLE POST (M11) ─────────────────────────────────────────────
    public Map<String, Object> getPost(Long postId, String viewerEmail) {
        CommunityPost post = findPost(postId);
        return toPostMap(post, viewerEmail);
    }

    // ── DELETE COMMENT (M9) ───────────────────────────────────────────────
    public void deleteComment(String email, Long commentId) {
        PostComment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        if (!comment.getAuthorEmail().equals(email))
            throw new SecurityException("You can only delete your own comments");
        commentRepository.deleteById(commentId);
    }

    // ── PRIVATE HELPERS ───────────────────────────────────────────────────
    private Map<String, Object> toPostMap(CommunityPost p, String viewerEmail) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",           p.getId());
        m.put("title",        p.getTitle() != null ? p.getTitle() : "");
        m.put("authorName",   p.getAuthorName());
        // H2: authorEmail removed from public response (privacy — enables harvesting/phishing)
        // isOwner flag lets the frontend show delete button without exposing email
        m.put("isOwner",      viewerEmail != null && viewerEmail.equals(p.getAuthorEmail()));
        m.put("content",      p.getContent());
        m.put("tags",         p.getTags() != null && !p.getTags().isBlank()
                              ? Arrays.asList(p.getTags().split(",")) : Collections.emptyList());
        m.put("likes",        p.getLikes());
        m.put("dislikes",     p.getDislikes());
        m.put("createdAt",    p.getCreatedAt() != null ? p.getCreatedAt().format(FMT) : "");
        m.put("photo",        p.getPhotoData() != null ? p.getPhotoData() : "");
        m.put("commentCount", commentRepository.countByPostId(p.getId()));

        // Viewer's reaction
        String userReaction = null;
        if (viewerEmail != null) {
            Optional<PostLike> pl = likeRepository.findByPostIdAndUserEmail(p.getId(), viewerEmail);
            if (pl.isPresent()) userReaction = pl.get().getReaction();
        }
        m.put("userReaction",  userReaction);
        m.put("userLiked",    "LIKE".equals(userReaction));
        m.put("userDisliked", "DISLIKE".equals(userReaction));
        return m;
    }

    private Map<String, Object> toCommentMap(PostComment c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",          c.getId());
        m.put("postId",      c.getPostId());
        m.put("authorName",  c.getAuthorName());
        // H2: authorEmail removed from public response
        m.put("content",     c.getContent());
        m.put("createdAt",   c.getCreatedAt() != null
                             ? c.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM")) : "");
        return m;
    }

    private CommunityPost findPost(Long id) {
        return postRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Post not found"));
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    // ── HTML ESCAPE (prevents XSS) ─────────────────────────────────────────
    private String escapeHtml(String input) {
        if (input == null) return null;
        return input
            .replace("&",  "&amp;")
            .replace("<",  "&lt;")
            .replace(">",  "&gt;")
            .replace("\"", "&quot;")
            .replace("'",  "&#x27;");
    }
}
