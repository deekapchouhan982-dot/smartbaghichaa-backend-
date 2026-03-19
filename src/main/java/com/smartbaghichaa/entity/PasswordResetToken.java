package com.smartbaghichaa.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    public PasswordResetToken() {}

    public PasswordResetToken(Long id, String email, String token, LocalDateTime expiresAt) {
        this.id = id; this.email = email; this.token = token; this.expiresAt = expiresAt;
    }

    public Long getId()                        { return id; }
    public void setId(Long id)                 { this.id = id; }
    public String getEmail()                   { return email; }
    public void setEmail(String email)         { this.email = email; }
    public String getToken()                   { return token; }
    public void setToken(String token)         { this.token = token; }
    public LocalDateTime getExpiresAt()        { return expiresAt; }
    public void setExpiresAt(LocalDateTime t)  { this.expiresAt = t; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id; private String email; private String token; private LocalDateTime expiresAt;
        public Builder id(Long id)             { this.id = id; return this; }
        public Builder email(String email)     { this.email = email; return this; }
        public Builder token(String token)     { this.token = token; return this; }
        public Builder expiresAt(LocalDateTime t){ this.expiresAt = t; return this; }
        public PasswordResetToken build()      { return new PasswordResetToken(id, email, token, expiresAt); }
    }
}
