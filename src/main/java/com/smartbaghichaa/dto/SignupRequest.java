package com.smartbaghichaa.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SignupRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 60, message = "Name must be 2–60 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "OTP is required")
    private String otp;

    public String getName()            { return name; }
    public void setName(String name)   { this.name = name; }
    public String getEmail()           { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword()        { return password; }
    public void setPassword(String pw) { this.password = pw; }
    public String getCity()            { return city; }
    public void setCity(String city)   { this.city = city; }
    public String getOtp()             { return otp; }
    public void setOtp(String otp)     { this.otp = otp; }
}
