package com.smartbaghichaa.dto;

import java.time.LocalDateTime;

public class AuthResponse {
    private String token;
    private String name;
    private String email;
    private String city;
    private LocalDateTime joinedAt;

    public AuthResponse() {}

    public AuthResponse(String token, String name, String email, String city, LocalDateTime joinedAt) {
        this.token = token; this.name = name; this.email = email;
        this.city = city; this.joinedAt = joinedAt;
    }

    public String getToken()                   { return token; }
    public void setToken(String token)         { this.token = token; }
    public String getName()                    { return name; }
    public void setName(String name)           { this.name = name; }
    public String getEmail()                   { return email; }
    public void setEmail(String email)         { this.email = email; }
    public String getCity()                    { return city; }
    public void setCity(String city)           { this.city = city; }
    public LocalDateTime getJoinedAt()         { return joinedAt; }
    public void setJoinedAt(LocalDateTime t)   { this.joinedAt = t; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String token, name, email, city;
        private LocalDateTime joinedAt;
        public Builder token(String t)         { this.token = t; return this; }
        public Builder name(String n)          { this.name = n; return this; }
        public Builder email(String e)         { this.email = e; return this; }
        public Builder city(String c)          { this.city = c; return this; }
        public Builder joinedAt(LocalDateTime t){ this.joinedAt = t; return this; }
        public AuthResponse build()            { return new AuthResponse(token, name, email, city, joinedAt); }
    }
}
