package com.smartbaghichaa.dto;

import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {

    @Size(min = 2, max = 60, message = "Name must be 2–60 characters")
    private String name;

    @Size(max = 100, message = "City must be under 100 characters")
    private String city;

    private String currentPassword;  // C2: required when changing password

    @Size(min = 8, max = 100, message = "New password must be at least 8 characters")
    private String newPassword;

    public String getName()                    { return name; }
    public void setName(String name)           { this.name = name; }
    public String getCity()                    { return city; }
    public void setCity(String city)           { this.city = city; }
    public String getCurrentPassword()         { return currentPassword; }
    public void setCurrentPassword(String pw)  { this.currentPassword = pw; }
    public String getNewPassword()             { return newPassword; }
    public void setNewPassword(String pw)      { this.newPassword = pw; }
}
