package com.smartbaghichaa.dto;

public class ContactMessageDto {
    private String name;
    private String email;
    private String subject;
    private String message;

    public String getName()              { return name; }
    public void setName(String name)     { this.name = name; }
    public String getEmail()             { return email; }
    public void setEmail(String email)   { this.email = email; }
    public String getSubject()           { return subject; }
    public void setSubject(String s)     { this.subject = s; }
    public String getMessage()           { return message; }
    public void setMessage(String m)     { this.message = m; }
}
