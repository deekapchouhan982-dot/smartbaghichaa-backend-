package com.smartbaghichaa.service;

import com.smartbaghichaa.entity.Reminder;
import com.smartbaghichaa.entity.User;
import com.smartbaghichaa.repository.EmailOtpRepository;
import com.smartbaghichaa.repository.PasswordResetTokenRepository;
import com.smartbaghichaa.repository.ReminderRepository;
import com.smartbaghichaa.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ReminderScheduler {

    @Autowired private ReminderRepository           reminderRepository;
    @Autowired private UserRepository               userRepository;
    @Autowired private EmailService                 emailService;
    @Autowired private EmailOtpRepository           emailOtpRepository;
    @Autowired private PasswordResetTokenRepository tokenRepository;

    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

    // Slot index → friendly task description
    private static final String[] SLOT_LABELS = {
        "Water your plants",
        "Check sunlight exposure",
        "Apply fertiliser if due",
        "Log today's plant growth",
        "Inspect for pests or disease",
        "Check if any plant is harvest-ready"
    };

    // Runs every minute at :00 seconds
    @Scheduled(cron = "0 * * * * *")
    public void sendDueReminders() {
        String nowHHmm = LocalTime.now().format(HH_MM);

        List<Reminder> enabled = reminderRepository.findAllByEnabled(true);
        for (Reminder r : enabled) {
            boolean isMorning = nowHHmm.equals(r.getMorningTime());
            boolean isEvening = nowHHmm.equals(r.getEveningTime());
            if (!isMorning && !isEvening) continue;

            Optional<User> optUser = userRepository.findById(r.getUserId());
            if (optUser.isEmpty()) continue;

            User user = optUser.get();
            String timeLabel = isMorning ? "Morning" : "Evening";
            List<String> tasks = buildTaskList(r, isMorning);
            if (tasks.isEmpty()) continue;

            emailService.sendReminderEmail(user.getName(), user.getEmail(), timeLabel, tasks);
            System.out.println("[ReminderScheduler] Sent " + timeLabel
                + " reminder to " + user.getEmail() + " at " + nowHHmm);
        }
    }

    // Runs once daily at 02:00 AM — purges expired OTPs and reset tokens
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        emailOtpRepository.deleteAllExpired(now);
        tokenRepository.deleteAllExpired(now);
        System.out.println("[ReminderScheduler] Nightly cleanup: expired OTPs and reset tokens purged at " + now);
    }

    private List<String> buildTaskList(Reminder r, boolean isMorning) {
        List<String> tasks = new ArrayList<>();
        Boolean[] slots = {r.getSlot1(), r.getSlot2(), r.getSlot3(),
                           r.getSlot4(), r.getSlot5(), r.getSlot6()};

        for (int i = 0; i < slots.length; i++) {
            if (!Boolean.TRUE.equals(slots[i])) continue;
            // slot2 (sunlight) and slot5 (pest) only in morning; slot6 (harvest) only in morning
            if (!isMorning && (i == 1 || i == 4 || i == 5)) continue;
            tasks.add(SLOT_LABELS[i]);
        }
        return tasks;
    }
}
