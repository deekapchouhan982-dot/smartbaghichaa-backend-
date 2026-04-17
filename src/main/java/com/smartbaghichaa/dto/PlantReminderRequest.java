package com.smartbaghichaa.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

public class PlantReminderRequest {

    @NotNull(message = "Plant ID is required")
    private Long userPlantId;

    @NotBlank(message = "Reminder type is required")
    private String reminderType;   // watering, fertilising, repotting, pruning, custom

    @NotBlank(message = "Frequency is required")
    private String frequency;      // daily, every_2_days, every_3_days, weekly, every_10_days, monthly

    private String nextReminderDate; // ISO date string yyyy-MM-dd
    private String customNote;
    private Boolean enabled;

    public Long getUserPlantId()              { return userPlantId; }
    public void setUserPlantId(Long u)        { this.userPlantId = u; }
    public String getReminderType()           { return reminderType; }
    public void setReminderType(String t)     { this.reminderType = t; }
    public String getFrequency()              { return frequency; }
    public void setFrequency(String f)        { this.frequency = f; }
    public String getNextReminderDate()       { return nextReminderDate; }
    public void setNextReminderDate(String d) { this.nextReminderDate = d; }
    public String getCustomNote()             { return customNote; }
    public void setCustomNote(String n)       { this.customNote = n; }
    public Boolean getEnabled()               { return enabled; }
    public void setEnabled(Boolean e)         { this.enabled = e; }
}
