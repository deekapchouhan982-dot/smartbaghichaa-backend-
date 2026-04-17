package com.smartbaghichaa.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "community_posts")
public class CommunityPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "author_email", nullable = false)
    private String authorEmail;

    @Column(name = "author_name", nullable = false)
    private String authorName;

    @Column(name = "title")
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    private String tags;

    private Integer likes = 0;
    private Integer dislikes = 0;

    @Column(name = "photo_data", columnDefinition = "LONGTEXT")
    private String photoData;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (likes == null) likes = 0;
        if (dislikes == null) dislikes = 0;
    }

    public CommunityPost() {}

    public Long getId()                        { return id; }
    public void setId(Long id)                 { this.id = id; }
    public String getAuthorEmail()             { return authorEmail; }
    public void setAuthorEmail(String e)       { this.authorEmail = e; }
    public String getAuthorName()              { return authorName; }
    public void setAuthorName(String n)        { this.authorName = n; }
    public String getTitle()                   { return title; }
    public void setTitle(String t)             { this.title = t; }
    public String getContent()                 { return content; }
    public void setContent(String c)           { this.content = c; }
    public String getTags()                    { return tags; }
    public void setTags(String t)              { this.tags = t; }
    public Integer getLikes()                  { return likes; }
    public void setLikes(Integer l)            { this.likes = l; }
    public Integer getDislikes()               { return dislikes; }
    public void setDislikes(Integer d)         { this.dislikes = d; }
    public String getPhotoData()               { return photoData; }
    public void setPhotoData(String p)         { this.photoData = p; }
    public LocalDateTime getCreatedAt()        { return createdAt; }
    public void setCreatedAt(LocalDateTime t)  { this.createdAt = t; }
}
