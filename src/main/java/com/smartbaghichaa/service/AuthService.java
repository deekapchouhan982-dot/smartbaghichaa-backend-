package com.smartbaghichaa.service;

import com.smartbaghichaa.dto.*;
import com.smartbaghichaa.entity.PasswordResetToken;
import com.smartbaghichaa.entity.User;
import com.smartbaghichaa.repository.PasswordResetTokenRepository;
import com.smartbaghichaa.repository.UserRepository;
import com.smartbaghichaa.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class AuthService {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordResetTokenRepository tokenRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private JavaMailSender mailSender;
    @Autowired private EmailService emailService;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$");

    // ─── SIGNUP ────────────────────────────────────────────────
    public AuthResponse signup(SignupRequest req) {
        String name  = req.getName()     != null ? req.getName().trim()     : "";
        String email = req.getEmail()    != null ? req.getEmail().trim().toLowerCase() : "";
        String city  = req.getCity()     != null ? req.getCity().trim()     : "";
        String pass  = req.getPassword() != null ? req.getPassword().trim() : "";

        if (name.isEmpty())
            throw new IllegalArgumentException("Name is required");
        if (name.length() > 100)
            throw new IllegalArgumentException("Name must be 100 characters or less");
        if (!EMAIL_PATTERN.matcher(email).matches())
            throw new IllegalArgumentException("Enter a valid email address");
        if (pass.length() < 6)
            throw new IllegalArgumentException("Password must be at least 6 characters");
        if (pass.length() > 128)
            throw new IllegalArgumentException("Password must be 128 characters or less");
        if (city.isEmpty())
            throw new IllegalArgumentException("City is required");
        if (city.length() > 100)
            throw new IllegalArgumentException("City name must be 100 characters or less");
        if (userRepository.existsByEmail(email))
            throw new IllegalStateException("Email already registered");

        User user = User.builder()
                .name(name).email(email)
                .password(passwordEncoder.encode(pass))
                .city(city).joinedAt(LocalDateTime.now())
                .build();
        userRepository.save(user);

        // Send welcome email asynchronously (non-blocking)
        emailService.sendWelcomeEmail(name, email, city);

        return AuthResponse.builder()
                .token(jwtUtil.generateToken(email))
                .name(user.getName()).email(user.getEmail())
                .city(user.getCity()).joinedAt(user.getJoinedAt())
                .build();
    }

    // ─── LOGIN ─────────────────────────────────────────────────
    public AuthResponse login(LoginRequest req) {
        String email = req.getEmail()    != null ? req.getEmail().trim().toLowerCase() : "";
        String pass  = req.getPassword() != null ? req.getPassword().trim()            : "";

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new SecurityException("Invalid email or password"));

        if (!passwordEncoder.matches(pass, user.getPassword()))
            throw new SecurityException("Invalid email or password");

        return AuthResponse.builder()
                .token(jwtUtil.generateToken(email))
                .name(user.getName()).email(user.getEmail())
                .city(user.getCity()).joinedAt(user.getJoinedAt())
                .build();
    }

    // ─── FORGOT PASSWORD ───────────────────────────────────────
    public String forgotPassword(String email) {
        email = email != null ? email.trim().toLowerCase() : "";

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No account with this email"));

        String resetToken = saveResetToken(email);
        sendResetEmail(user.getName(), email, resetToken);
        return "Reset instructions sent to your email";
    }

    @Transactional
    public String saveResetToken(String email) {
        tokenRepository.deleteByEmail(email);
        String resetToken = UUID.randomUUID().toString();
        PasswordResetToken prt = PasswordResetToken.builder()
                .email(email).token(resetToken)
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .build();
        tokenRepository.save(prt);
        return resetToken;
    }

    private void sendResetEmail(String name, String email, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Smart Baghichaa \u2014 Password Reset");
        message.setText(
                "Hello " + name + "!\n\n" +
                "You requested a password reset for your Smart Baghichaa account.\n\n" +
                "Click this link to reset your password:\n" +
                "http://localhost:8080/reset?token=" + token + "\n\n" +
                "This link expires in 30 minutes.\n\n" +
                "If you didn't request this, ignore this email.\n\n" +
                "\u2014 Smart Baghichaa Team \uD83C\uDF31"
        );
        mailSender.send(message);
    }

    // ─── UPDATE PROFILE ────────────────────────────────────────
    public AuthResponse updateProfile(String email, UpdateProfileRequest req) {
        String newName = req.getName()        != null ? req.getName().trim()        : "";
        String newCity = req.getCity()        != null ? req.getCity().trim()        : "";
        String newPass = req.getNewPassword() != null ? req.getNewPassword().trim() : "";

        if (newName.isEmpty())
            throw new IllegalArgumentException("Name is required");
        if (newCity.isEmpty())
            throw new IllegalArgumentException("City is required");
        if (!newPass.isEmpty() && newPass.length() < 6)
            throw new IllegalArgumentException("New password must be at least 6 characters");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setName(newName);
        user.setCity(newCity);
        if (!newPass.isEmpty()) user.setPassword(passwordEncoder.encode(newPass));
        userRepository.save(user);

        return AuthResponse.builder()
                .token(jwtUtil.generateToken(email))
                .name(user.getName()).email(user.getEmail())
                .city(user.getCity()).joinedAt(user.getJoinedAt())
                .build();
    }

    // ─── GET ME ────────────────────────────────────────────────
    public AuthResponse getMe(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return AuthResponse.builder()
                .token(null)
                .name(user.getName()).email(user.getEmail())
                .city(user.getCity()).joinedAt(user.getJoinedAt())
                .build();
    }
}
