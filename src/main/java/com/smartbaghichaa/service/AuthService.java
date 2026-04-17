package com.smartbaghichaa.service;

import com.smartbaghichaa.dto.*;
import com.smartbaghichaa.entity.EmailOtp;
import com.smartbaghichaa.entity.PasswordResetToken;
import com.smartbaghichaa.entity.User;
import com.smartbaghichaa.entity.UserPlant;
import com.smartbaghichaa.repository.*;

import com.smartbaghichaa.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Service
public class AuthService {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordResetTokenRepository tokenRepository;
    @Autowired private EmailOtpRepository emailOtpRepository;
    @Autowired private ReminderRepository reminderRepository;
    @Autowired private PlantAnalysisRepository plantAnalysisRepository;
    @Autowired private UserPlantRepository userPlantRepository;
    @Autowired private GrowthLogRepository growthLogRepository;
    @Autowired private CommunityPostRepository communityPostRepository;
    @Autowired private PostLikeRepository postLikeRepository;
    @Autowired private PostCommentRepository postCommentRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private EmailService emailService;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$");
    private static final SecureRandom RANDOM = new SecureRandom();

    private static final int OTP_RATE_LIMIT    = 3;
    private static final int OTP_WINDOW_MINUTES = 10;

    // ── Login brute-force protection ────────────────────────────────────────
    private static final int  LOGIN_MAX_ATTEMPTS  = 5;
    private static final long LOGIN_WINDOW_MS      = 15 * 60 * 1000L; // 15 minutes
    private final Map<String, List<Long>> loginAttempts = new ConcurrentHashMap<>();

    // ─── SEND SIGNUP OTP ───────────────────────────────────────
    @Transactional
    public String sendSignupOtp(String email, String name) {
        email = email != null ? email.trim().toLowerCase() : "";
        if (!EMAIL_PATTERN.matcher(email).matches())
            throw new IllegalArgumentException("Enter a valid email address");
        if (userRepository.existsByEmail(email))
            throw new IllegalStateException("Email already registered");

        long recentCount = emailOtpRepository.countByEmailAndTypeAndCreatedAtAfter(
            email, "SIGNUP", LocalDateTime.now().minusMinutes(OTP_WINDOW_MINUTES));
        if (recentCount >= OTP_RATE_LIMIT)
            throw new IllegalStateException("Too many OTP requests. Please wait " + OTP_WINDOW_MINUTES + " minutes before trying again.");

        String otp = String.format("%06d", 100000 + RANDOM.nextInt(900000));
        // Don't delete before saving — deletions would reset the count.
        // Clean up only entries older than the rate-limit window.
        emailOtpRepository.deleteOldEntries(email, "SIGNUP",
            LocalDateTime.now().minusMinutes(OTP_WINDOW_MINUTES));
        EmailOtp eo = new EmailOtp();
        eo.setEmail(email);
        eo.setOtp(otp);
        eo.setType("SIGNUP");
        eo.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        emailOtpRepository.save(eo);

        emailService.sendOtpEmail(name, email, otp, "SIGNUP");
        return "OTP sent to " + email;
    }

    // ─── SIGNUP ────────────────────────────────────────────────
    @Transactional
    public AuthResponse signup(SignupRequest req) {
        String name  = req.getName()     != null ? req.getName().trim()     : "";
        String email = req.getEmail()    != null ? req.getEmail().trim().toLowerCase() : "";
        String city  = req.getCity()     != null ? req.getCity().trim()     : "";
        String pass  = req.getPassword() != null ? req.getPassword().trim() : "";
        String otp   = req.getOtp()      != null ? req.getOtp().trim()      : "";

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

        // OTP verification is mandatory
        if (otp.isEmpty())
            throw new IllegalArgumentException("Email verification is required — please request an OTP first");
        EmailOtp stored = emailOtpRepository
            .findTopByEmailAndTypeOrderByIdDesc(email, "SIGNUP")
            .orElseThrow(() -> new IllegalArgumentException("OTP not found — please request a new one"));
        if (LocalDateTime.now().isAfter(stored.getExpiresAt()))
            throw new IllegalArgumentException("OTP has expired — please request a new one");
        if (!stored.getOtp().equals(otp))
            throw new IllegalArgumentException("Incorrect OTP — please try again");
        emailOtpRepository.deleteByEmailAndType(email, "SIGNUP");

        User user = User.builder()
                .name(name).email(email)
                .password(passwordEncoder.encode(pass))
                .city(city).joinedAt(LocalDateTime.now())
                .build();
        userRepository.save(user);

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

        // Brute-force protection: block after 5 failures within 15 min
        long now = System.currentTimeMillis();
        List<Long> attempts = loginAttempts.computeIfAbsent(email, k -> new ArrayList<>());
        synchronized (attempts) {
            attempts.removeIf(t -> now - t > LOGIN_WINDOW_MS);
            if (attempts.size() >= LOGIN_MAX_ATTEMPTS) {
                long waitMin = Math.max(1, (LOGIN_WINDOW_MS - (now - attempts.get(0))) / 60000);
                throw new IllegalStateException(
                    "Too many failed login attempts. Try again in " + waitMin + " minute(s).");
            }
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || !passwordEncoder.matches(pass, user.getPassword())) {
            synchronized (attempts) { attempts.add(now); }
            throw new SecurityException("Invalid email or password");
        }
        synchronized (attempts) { attempts.clear(); }

        return AuthResponse.builder()
                .token(jwtUtil.generateToken(email))
                .name(user.getName()).email(user.getEmail())
                .city(user.getCity()).joinedAt(user.getJoinedAt())
                .build();
    }

    // ─── FORGOT PASSWORD ───────────────────────────────────────
    @Transactional
    public String forgotPassword(String email) {
        email = email != null ? email.trim().toLowerCase() : "";

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No account with this email"));

        String resetToken = saveResetToken(email);
        // Send email asynchronously — token is already saved, so email failure won't break the flow
        emailService.sendResetEmail(user.getName(), email, resetToken);
        return "Reset instructions sent to your email";
    }

    // ─── SEND FORGOT-PASSWORD OTP ──────────────────────────────
    @Transactional
    public String sendForgotOtp(String email) {
        email = email != null ? email.trim().toLowerCase() : "";
        if (!EMAIL_PATTERN.matcher(email).matches())
            throw new IllegalArgumentException("Enter a valid email address");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No account with this email"));

        long recentCount = emailOtpRepository.countByEmailAndTypeAndCreatedAtAfter(
            email, "FORGOT", LocalDateTime.now().minusMinutes(OTP_WINDOW_MINUTES));
        if (recentCount >= OTP_RATE_LIMIT)
            throw new IllegalStateException("Too many OTP requests. Please wait " + OTP_WINDOW_MINUTES + " minutes before trying again.");

        String otp = String.format("%06d", 100000 + RANDOM.nextInt(900000));
        emailOtpRepository.deleteOldEntries(email, "FORGOT",
            LocalDateTime.now().minusMinutes(OTP_WINDOW_MINUTES));
        EmailOtp eo = new EmailOtp();
        eo.setEmail(email);
        eo.setOtp(otp);
        eo.setType("FORGOT");
        eo.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        emailOtpRepository.save(eo);

        emailService.sendOtpEmail(user.getName(), email, otp, "FORGOT");
        return "OTP sent to " + email;
    }

    // ─── RESET PASSWORD WITH OTP ───────────────────────────────
    @Transactional
    public String resetPasswordWithOtp(ResetPasswordRequest req) {
        String email   = req.getEmail()       != null ? req.getEmail().trim().toLowerCase() : "";
        String otp     = req.getOtp()         != null ? req.getOtp().trim()                 : "";
        String newPass = req.getNewPassword() != null ? req.getNewPassword().trim()         : "";

        if (newPass.length() < 6)
            throw new IllegalArgumentException("Password must be at least 6 characters");

        EmailOtp stored = emailOtpRepository
            .findTopByEmailAndTypeOrderByIdDesc(email, "FORGOT")
            .orElseThrow(() -> new IllegalArgumentException("OTP not found — please request a new one"));
        if (LocalDateTime.now().isAfter(stored.getExpiresAt()))
            throw new IllegalArgumentException("OTP has expired — please request a new one");
        if (!stored.getOtp().equals(otp))
            throw new IllegalArgumentException("Incorrect OTP — please try again");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setPassword(passwordEncoder.encode(newPass));
        user.setPasswordChangedAt(LocalDateTime.now()); // H1: invalidate old tokens
        userRepository.save(user);
        emailOtpRepository.deleteByEmailAndType(email, "FORGOT");
        return "Password reset successfully";
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

    // ─── UPDATE PROFILE ────────────────────────────────────────
    public AuthResponse updateProfile(String email, UpdateProfileRequest req) {
        String newName   = req.getName()            != null ? req.getName().trim()            : "";
        String newCity   = req.getCity()            != null ? req.getCity().trim()            : "";
        String newPass   = req.getNewPassword()     != null ? req.getNewPassword().trim()     : "";
        String currPass  = req.getCurrentPassword() != null ? req.getCurrentPassword().trim() : "";

        if (newName.isEmpty())
            throw new IllegalArgumentException("Name is required");
        if (newCity.isEmpty())
            throw new IllegalArgumentException("City is required");
        if (!newPass.isEmpty() && newPass.length() < 6)
            throw new IllegalArgumentException("New password must be at least 6 characters");
        // C2: Require current password when changing password
        if (!newPass.isEmpty() && currPass.isEmpty())
            throw new IllegalArgumentException("Current password is required to set a new password");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // C2: Verify current password matches stored hash
        if (!newPass.isEmpty() && !passwordEncoder.matches(currPass, user.getPassword()))
            throw new SecurityException("Current password is incorrect");

        user.setName(newName);
        user.setCity(newCity);
        if (!newPass.isEmpty()) {
            user.setPassword(passwordEncoder.encode(newPass));
            // H1: Record password change time so old tokens are invalidated
            user.setPasswordChangedAt(LocalDateTime.now());
        }
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

    // ─── DELETE ACCOUNT ────────────────────────────────────────
    @Transactional
    public void deleteAccount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Long userId = user.getId();

        // Clear OTPs and reset tokens
        emailOtpRepository.deleteByEmail(email);
        tokenRepository.deleteByEmail(email);

        // Delete reminders
        reminderRepository.findByUserId(userId).ifPresent(reminderRepository::delete);

        // Delete AI plant analyses
        plantAnalysisRepository.deleteByUserEmail(email);

        // Delete growth logs for all user plants, then delete plants
        List<UserPlant> plants = userPlantRepository.findByUserId(userId);
        if (!plants.isEmpty()) {
            List<Long> plantIds = plants.stream().map(UserPlant::getId).collect(java.util.stream.Collectors.toList());
            growthLogRepository.deleteByUserPlantIdIn(plantIds);
            userPlantRepository.deleteAll(plants);
        }

        // Delete community posts (with their likes and comments)
        List<com.smartbaghichaa.entity.CommunityPost> posts = communityPostRepository.findByAuthorEmailOrderByCreatedAtDesc(email);
        for (com.smartbaghichaa.entity.CommunityPost post : posts) {
            postLikeRepository.deleteByPostId(post.getId());
            postCommentRepository.deleteByPostId(post.getId());
        }
        communityPostRepository.deleteAll(posts);

        // Delete the user
        userRepository.delete(user);
    }
}
