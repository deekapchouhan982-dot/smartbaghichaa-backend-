package com.smartbaghichaa.dto;

public class UpdateProfileRequest {
    private String name;
    private String city;
    private String newPassword;

    public String getName()                  { return name; }
    public void setName(String name)         { this.name = name; }
    public String getCity()                  { return city; }
    public void setCity(String city)         { this.city = city; }
    public String getNewPassword()           { return newPassword; }
    public void setNewPassword(String pw)    { this.newPassword = pw; }
}
