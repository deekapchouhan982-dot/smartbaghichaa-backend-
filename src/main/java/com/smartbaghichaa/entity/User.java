package com.smartbaghichaa.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String city;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    // H1: used to invalidate tokens issued before a password change
    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @PrePersist
    protected void onCreate() {
        if (joinedAt == null) joinedAt = LocalDateTime.now();
    }

    public User() {}

    public User(Long id, String name, String email, String password, String city, LocalDateTime joinedAt) {
        this.id = id; this.name = name; this.email = email;
        this.password = password; this.city = city; this.joinedAt = joinedAt;
    }

    public Long getId()                    { return id; }
    public void setId(Long id)             { this.id = id; }
    public String getName()                { return name; }
    public void setName(String name)       { this.name = name; }
    public String getEmail()               { return email; }
    public void setEmail(String email)     { this.email = email; }
    public String getPassword()            { return password; }
    public void setPassword(String password){ this.password = password; }
    public String getCity()                { return city; }
    public void setCity(String city)       { this.city = city; }
    public LocalDateTime getJoinedAt()     { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
    public LocalDateTime getPasswordChangedAt()                   { return passwordChangedAt; }
    public void setPasswordChangedAt(LocalDateTime t)             { this.passwordChangedAt = t; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id; private String name; private String email;
        private String password; private String city; private LocalDateTime joinedAt;

        public Builder id(Long id)             { this.id = id; return this; }
        public Builder name(String name)       { this.name = name; return this; }
        public Builder email(String email)     { this.email = email; return this; }
        public Builder password(String pw)     { this.password = pw; return this; }
        public Builder city(String city)       { this.city = city; return this; }
        public Builder joinedAt(LocalDateTime t){ this.joinedAt = t; return this; }
        public User build() {
            return new User(id, name, email, password, city, joinedAt);
        }
    }
}
