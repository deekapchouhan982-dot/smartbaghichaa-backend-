package com.smartbaghichaa.service;

import com.smartbaghichaa.entity.Reminder;
import com.smartbaghichaa.entity.User;
import com.smartbaghichaa.repository.ReminderRepository;
import com.smartbaghichaa.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class ReminderService {

    @Autowired
    private ReminderRepository reminderRepository;

    @Autowired
    private UserRepository userRepository;

    // ── GET /api/reminders ──────────────────────────────────────────────────
    public Map<String, Object> getReminders(String email) {
        User user = findUser(email);
        Optional<Reminder> opt = reminderRepository.findByUserId(user.getId());
        return opt.map(this::toMap).orElseGet(this::defaultReminder);
    }

    // ── POST /api/reminders ─────────────────────────────────────────────────
    public Map<String, Object> saveReminders(String email, Map<String, Object> body) {
        User user = findUser(email);
        Reminder r = reminderRepository.findByUserId(user.getId()).orElse(new Reminder());
        r.setUserId(user.getId());
        r.setMorningTime(getString(body, "morningTime", "07:00"));
        r.setEveningTime(getString(body, "eveningTime", "18:00"));
        r.setEnabled(getBool(body, "enabled", true));
        r.setSlot1(getBool(body, "slot1", true));
        r.setSlot2(getBool(body, "slot2", true));
        r.setSlot3(getBool(body, "slot3", true));
        r.setSlot4(getBool(body, "slot4", true));
        r.setSlot5(getBool(body, "slot5", false));
        r.setSlot6(getBool(body, "slot6", false));
        r.setUpdatedAt(LocalDateTime.now());

        Reminder saved = reminderRepository.save(r);
        Map<String, Object> m = toMap(saved);
        m.put("message", "Reminders saved!");
        return m;
    }

    // ── Helpers ─────────────────────────────────────────────────────────────
    private Map<String, Object> toMap(Reminder r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",          r.getId());
        m.put("morningTime", r.getMorningTime() != null ? r.getMorningTime() : "07:00");
        m.put("eveningTime", r.getEveningTime() != null ? r.getEveningTime() : "18:00");
        m.put("enabled",     Boolean.TRUE.equals(r.getEnabled()));
        m.put("slot1",       Boolean.TRUE.equals(r.getSlot1()));
        m.put("slot2",       Boolean.TRUE.equals(r.getSlot2()));
        m.put("slot3",       Boolean.TRUE.equals(r.getSlot3()));
        m.put("slot4",       Boolean.TRUE.equals(r.getSlot4()));
        m.put("slot5",       Boolean.TRUE.equals(r.getSlot5()));
        m.put("slot6",       Boolean.TRUE.equals(r.getSlot6()));
        return m;
    }

    private Map<String, Object> defaultReminder() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",          null);
        m.put("morningTime", "07:00");
        m.put("eveningTime", "18:00");
        m.put("enabled",     false);
        m.put("slot1",       true);
        m.put("slot2",       true);
        m.put("slot3",       true);
        m.put("slot4",       true);
        m.put("slot5",       false);
        m.put("slot6",       false);
        return m;
    }

    private String getString(Map<String, Object> body, String key, String def) {
        Object v = body.get(key);
        return (v instanceof String && !((String)v).isBlank()) ? (String) v : def;
    }

    private boolean getBool(Map<String, Object> body, String key, boolean def) {
        Object v = body.get(key);
        if (v instanceof Boolean) return (Boolean) v;
        if (v instanceof String)  return Boolean.parseBoolean((String) v);
        return def;
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
