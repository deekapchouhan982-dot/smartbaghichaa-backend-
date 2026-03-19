package com.smartbaghichaa.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "post_comments")
public class PostComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "author_name", nullable = false)
    private String authorName;

    @Column(name = "author_email", nullable = false)
    private String authorEmail;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public PostComment() {}

    public Long getId()                        { return id; }
    public void setId(Long id)                 { this.id = id; }
    public Long getPostId()                    { return postId; }
    public void setPostId(Long pid)            { this.postId = pid; }
    public String getAuthorName()              { return authorName; }
    public void setAuthorName(String n)        { this.authorName = n; }
    public String getAuthorEmail()             { return authorEmail; }
    public void setAuthorEmail(String e)       { this.authorEmail = e; }
    public String getContent()                 { return content; }
    public void setContent(String c)           { this.content = c; }
    public LocalDateTime getCreatedAt()        { return createdAt; }
    public void setCreatedAt(LocalDateTime t)  { this.createdAt = t; }
}
