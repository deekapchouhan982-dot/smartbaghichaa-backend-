package com.smartbaghichaa.service;

import com.smartbaghichaa.dto.PlantReminderRequest;
import com.smartbaghichaa.entity.PlantReminder;
import com.smartbaghichaa.entity.User;
import com.smartbaghichaa.entity.UserPlant;
import com.smartbaghichaa.repository.PlantReminderRepository;
import com.smartbaghichaa.repository.UserPlantRepository;
import com.smartbaghichaa.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ISSUE-10: Plant-specific reminder scheduling service.
 */
@Service
public class PlantReminderService {

    @Autowired private PlantReminderRepository reminderRepo;
    @Autowired private UserRepository userRepository;
    @Autowired private UserPlantRepository userPlantRepository;

    private static final Set<String> VALID_TYPES = Set.of(
        "watering", "fertilising", "repotting", "pruning", "custom");
    private static final Set<String> VALID_FREQS = Set.of(
        "daily", "every_2_days", "every_3_days", "weekly", "every_10_days", "monthly");

    // ── GET all reminders for user ────────────────────────────────────────────
    public List<Map<String, Object>> getReminders(String email) {
        User user = findUser(email);
        return reminderRepo.findByUserId(user.getId()).stream()
            .map(r -> toMap(r, userPlantRepository))
            .collect(Collectors.toList());
    }

    // ── GET reminders for a specific plant ───────────────────────────────────
    public List<Map<String, Object>> getRemindersForPlant(String email, Long plantId) {
        User user = findUser(email);
        userPlantRepository.findByIdAndUserId(plantId, user.getId())
            .orElseThrow(() -> new IllegalArgumentException("Plant not found in your garden"));
        return reminderRepo.findByUserPlantIdAndUserId(plantId, user.getId()).stream()
            .map(r -> toMap(r, userPlantRepository))
            .collect(Collectors.toList());
    }

    // ── CREATE reminder ───────────────────────────────────────────────────────
    public Map<String, Object> createReminder(String email, PlantReminderRequest req) {
        User user = findUser(email);
        normalize(req);
        validate(req);

        userPlantRepository.findByIdAndUserId(req.getUserPlantId(), user.getId())
            .orElseThrow(() -> new IllegalArgumentException("Plant not found in your garden"));

        PlantReminder r = new PlantReminder();
        r.setUserId(user.getId());
        r.setUserPlantId(req.getUserPlantId());
        r.setReminderType(req.getReminderType());
        r.setFrequency(req.getFrequency());
        r.setCustomNote(req.getCustomNote());
        r.setEnabled(req.getEnabled() != null ? req.getEnabled() : true);
        r.setNextReminderDate(parseDate(req.getNextReminderDate()));

        PlantReminder saved = reminderRepo.save(r);
        Map<String, Object> m = toMap(saved, userPlantRepository);
        m.put("message", "Reminder created!");
        return m;
    }

    // ── UPDATE reminder ───────────────────────────────────────────────────────
    public Map<String, Object> updateReminder(String email, Long reminderId, PlantReminderRequest req) {
        User user = findUser(email);
        normalize(req);
        PlantReminder r = reminderRepo.findById(reminderId)
            .orElseThrow(() -> new IllegalArgumentException("Reminder not found"));
        if (!r.getUserId().equals(user.getId()))
            throw new SecurityException("Access denied");

        if (req.getReminderType() != null) r.setReminderType(req.getReminderType());
        if (req.getFrequency() != null)    r.setFrequency(req.getFrequency());
        if (req.getCustomNote() != null)   r.setCustomNote(req.getCustomNote());
        if (req.getEnabled() != null)      r.setEnabled(req.getEnabled());
        if (req.getNextReminderDate() != null) r.setNextReminderDate(parseDate(req.getNextReminderDate()));

        PlantReminder saved = reminderRepo.save(r);
        Map<String, Object> m = toMap(saved, userPlantRepository);
        m.put("message", "Reminder updated!");
        return m;
    }

    // ── DELETE reminder ───────────────────────────────────────────────────────
    public void deleteReminder(String email, Long reminderId) {
        User user = findUser(email);
        PlantReminder r = reminderRepo.findById(reminderId)
            .orElseThrow(() -> new IllegalArgumentException("Reminder not found"));
        if (!r.getUserId().equals(user.getId()))
            throw new SecurityException("Access denied");
        reminderRepo.deleteById(reminderId);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void normalize(PlantReminderRequest req) {
        if (req.getReminderType() != null)
            req.setReminderType(req.getReminderType().toLowerCase().trim());
        if (req.getFrequency() != null)
            req.setFrequency(req.getFrequency().toLowerCase().trim());
    }

    private void validate(PlantReminderRequest req) {
        if (req.getUserPlantId() == null)
            throw new IllegalArgumentException("userPlantId is required");
        if (req.getReminderType() == null || !VALID_TYPES.contains(req.getReminderType()))
            throw new IllegalArgumentException("reminderType must be one of: " + VALID_TYPES);
        if (req.getFrequency() == null || !VALID_FREQS.contains(req.getFrequency()))
            throw new IllegalArgumentException("frequency must be one of: " + VALID_FREQS);
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return LocalDate.now();
        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    private Map<String, Object> toMap(PlantReminder r, UserPlantRepository plantRepo) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",               r.getId());
        m.put("userPlantId",      r.getUserPlantId());
        m.put("reminderType",     r.getReminderType());
        m.put("frequency",        r.getFrequency());
        m.put("nextReminderDate", r.getNextReminderDate() != null ? r.getNextReminderDate().toString() : null);
        m.put("customNote",       r.getCustomNote());
        m.put("enabled",          Boolean.TRUE.equals(r.getEnabled()));
        m.put("createdAt",        r.getCreatedAt() != null ? r.getCreatedAt().toString() : null);
        // Include plant name for convenience
        plantRepo.findById(r.getUserPlantId()).ifPresent(up -> {
            m.put("plantName",  up.getPlantName());
            m.put("plantEmoji", up.getPlantEmoji());
        });
        return m;
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
