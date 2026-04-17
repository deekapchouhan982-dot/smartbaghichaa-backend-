package com.smartbaghichaa.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ISSUE-10: Plant-specific reminder scheduling.
 * Allows per-plant care schedules (e.g., water Tomato Mon/Wed/Fri, water Cactus every 10 days).
 */
@Entity
@Table(name = "plant_reminders")
public class PlantReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_plant_id", nullable = false)
    private Long userPlantId;

    // Type: "watering", "fertilising", "repotting", "pruning", "custom"
    @Column(name = "reminder_type", nullable = false)
    private String reminderType;

    // "daily", "every_2_days", "every_3_days", "weekly", "every_10_days", "monthly", "custom"
    @Column(name = "frequency", nullable = false)
    private String frequency;

    @Column(name = "next_reminder_date")
    private LocalDate nextReminderDate;

    @Column(name = "custom_note")
    private String customNote;

    @Column(name = "enabled")
    private Boolean enabled = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (enabled == null) enabled = true;
    }

    public PlantReminder() {}

    public Long getId()                          { return id; }
    public void setId(Long id)                   { this.id = id; }
    public Long getUserId()                      { return userId; }
    public void setUserId(Long u)                { this.userId = u; }
    public Long getUserPlantId()                 { return userPlantId; }
    public void setUserPlantId(Long p)           { this.userPlantId = p; }
    public String getReminderType()              { return reminderType; }
    public void setReminderType(String t)        { this.reminderType = t; }
    public String getFrequency()                 { return frequency; }
    public void setFrequency(String f)           { this.frequency = f; }
    public LocalDate getNextReminderDate()       { return nextReminderDate; }
    public void setNextReminderDate(LocalDate d) { this.nextReminderDate = d; }
    public String getCustomNote()                { return customNote; }
    public void setCustomNote(String n)          { this.customNote = n; }
    public Boolean getEnabled()                  { return enabled; }
    public void setEnabled(Boolean e)            { this.enabled = e; }
    public LocalDateTime getCreatedAt()          { return createdAt; }
    public void setCreatedAt(LocalDateTime t)    { this.createdAt = t; }
}
