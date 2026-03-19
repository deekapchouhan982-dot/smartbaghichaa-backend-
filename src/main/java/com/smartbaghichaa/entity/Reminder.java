package com.smartbaghichaa.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reminders")
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "morning_time")
    private String morningTime;

    @Column(name = "evening_time")
    private String eveningTime;

    private Boolean enabled = true;

    // ── Reminder slots (slot1=water, slot2=sun, slot3=fertiliser,
    //                    slot4=log,   slot5=pest, slot6=harvest) ──
    @Column(name = "slot1") private Boolean slot1 = true;
    @Column(name = "slot2") private Boolean slot2 = true;
    @Column(name = "slot3") private Boolean slot3 = true;
    @Column(name = "slot4") private Boolean slot4 = true;
    @Column(name = "slot5") private Boolean slot5 = false;
    @Column(name = "slot6") private Boolean slot6 = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (enabled  == null) enabled   = true;
        if (slot1    == null) slot1     = true;
        if (slot2    == null) slot2     = true;
        if (slot3    == null) slot3     = true;
        if (slot4    == null) slot4     = true;
        if (slot5    == null) slot5     = false;
        if (slot6    == null) slot6     = false;
        updatedAt = createdAt;
    }

    public Reminder() {}

    public Long getId()                        { return id; }
    public void setId(Long id)                 { this.id = id; }
    public Long getUserId()                    { return userId; }
    public void setUserId(Long uid)            { this.userId = uid; }
    public String getMorningTime()             { return morningTime; }
    public void setMorningTime(String t)       { this.morningTime = t; }
    public String getEveningTime()             { return eveningTime; }
    public void setEveningTime(String t)       { this.eveningTime = t; }
    public Boolean getEnabled()                { return enabled; }
    public void setEnabled(Boolean e)          { this.enabled = e; }
    public Boolean getSlot1()                  { return slot1; }
    public void setSlot1(Boolean v)            { this.slot1 = v; }
    public Boolean getSlot2()                  { return slot2; }
    public void setSlot2(Boolean v)            { this.slot2 = v; }
    public Boolean getSlot3()                  { return slot3; }
    public void setSlot3(Boolean v)            { this.slot3 = v; }
    public Boolean getSlot4()                  { return slot4; }
    public void setSlot4(Boolean v)            { this.slot4 = v; }
    public Boolean getSlot5()                  { return slot5; }
    public void setSlot5(Boolean v)            { this.slot5 = v; }
    public Boolean getSlot6()                  { return slot6; }
    public void setSlot6(Boolean v)            { this.slot6 = v; }
    public LocalDateTime getCreatedAt()        { return createdAt; }
    public void setCreatedAt(LocalDateTime t)  { this.createdAt = t; }
    public LocalDateTime getUpdatedAt()        { return updatedAt; }
    public void setUpdatedAt(LocalDateTime t)  { this.updatedAt = t; }
}
