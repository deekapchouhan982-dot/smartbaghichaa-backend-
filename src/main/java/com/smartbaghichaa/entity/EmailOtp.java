package com.smartbaghichaa.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_otps")
public class EmailOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 6)
    private String otp;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public EmailOtp() {}

    public Long getId()                         { return id; }
    public void setId(Long id)                  { this.id = id; }
    public String getEmail()                    { return email; }
    public void setEmail(String email)          { this.email = email; }
    public String getOtp()                      { return otp; }
    public void setOtp(String otp)              { this.otp = otp; }
    public String getType()                     { return type; }
    public void setType(String type)            { this.type = type; }
    public LocalDateTime getExpiresAt()         { return expiresAt; }
    public void setExpiresAt(LocalDateTime e)   { this.expiresAt = e; }
    public LocalDateTime getCreatedAt()         { return createdAt; }
    public void setCreatedAt(LocalDateTime c)   { this.createdAt = c; }
}
