package com.smartbaghichaa.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_plants")
public class UserPlant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "plant_name", nullable = false)
    private String plantName;

    @Column(name = "plant_emoji")
    private String plantEmoji;

    @Column(name = "category")
    private String category;

    @Column(name = "added_at")
    private LocalDateTime addedAt;

    @PrePersist
    protected void onCreate() {
        if (addedAt == null) addedAt = LocalDateTime.now();
    }

    public UserPlant() {}

    public Long getId()                      { return id; }
    public void setId(Long id)               { this.id = id; }
    public Long getUserId()                  { return userId; }
    public void setUserId(Long userId)       { this.userId = userId; }
    public String getPlantName()             { return plantName; }
    public void setPlantName(String n)       { this.plantName = n; }
    public String getPlantEmoji()            { return plantEmoji; }
    public void setPlantEmoji(String e)      { this.plantEmoji = e; }
    public String getCategory()              { return category; }
    public void setCategory(String c)        { this.category = c; }
    public LocalDateTime getAddedAt()        { return addedAt; }
    public void setAddedAt(LocalDateTime t)  { this.addedAt = t; }
}
