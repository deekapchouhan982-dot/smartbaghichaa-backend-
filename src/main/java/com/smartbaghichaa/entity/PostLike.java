package com.smartbaghichaa.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "post_likes",
       uniqueConstraints = @UniqueConstraint(columnNames = {"post_id","user_email"}))
public class PostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(nullable = false)
    private String reaction; // "LIKE" or "DISLIKE"

    public PostLike() {}

    public PostLike(Long postId, String userEmail, String reaction) {
        this.postId = postId; this.userEmail = userEmail; this.reaction = reaction;
    }

    public Long getId()                    { return id; }
    public Long getPostId()                { return postId; }
    public void setPostId(Long p)          { this.postId = p; }
    public String getUserEmail()           { return userEmail; }
    public void setUserEmail(String e)     { this.userEmail = e; }
    public String getReaction()            { return reaction; }
    public void setReaction(String r)      { this.reaction = r; }
}
