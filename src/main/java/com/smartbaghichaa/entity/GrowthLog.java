package com.smartbaghichaa.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "growth_logs")
public class GrowthLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_plant_id", nullable = false)
    private Long userPlantId;

    private String stage;

    @Column(name = "health_score")
    private Integer healthScore;

    private String watering;
    private String sunlight;

    @Column(name = "soil_condition")
    private String soilCondition;

    @Column(name = "leaf_colour")
    private String leafColour;

    @Column(name = "stem_condition")
    private String stemCondition;

    private String fertiliser;

    @Column(columnDefinition = "TEXT")
    private String observations;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "smart_tip", columnDefinition = "TEXT")
    private String smartTip;

    @Column(name = "logged_at")
    private LocalDateTime loggedAt;

    @PrePersist
    protected void onCreate() {
        if (loggedAt == null) loggedAt = LocalDateTime.now();
    }

    public GrowthLog() {}

    public Long getId()                        { return id; }
    public void setId(Long id)                 { this.id = id; }
    public Long getUserPlantId()               { return userPlantId; }
    public void setUserPlantId(Long id)        { this.userPlantId = id; }
    public String getStage()                   { return stage; }
    public void setStage(String stage)         { this.stage = stage; }
    public Integer getHealthScore()            { return healthScore; }
    public void setHealthScore(Integer s)      { this.healthScore = s; }
    public String getWatering()                { return watering; }
    public void setWatering(String w)          { this.watering = w; }
    public String getSunlight()                { return sunlight; }
    public void setSunlight(String s)          { this.sunlight = s; }
    public String getSoilCondition()           { return soilCondition; }
    public void setSoilCondition(String s)     { this.soilCondition = s; }
    public String getLeafColour()              { return leafColour; }
    public void setLeafColour(String l)        { this.leafColour = l; }
    public String getStemCondition()           { return stemCondition; }
    public void setStemCondition(String s)     { this.stemCondition = s; }
    public String getFertiliser()              { return fertiliser; }
    public void setFertiliser(String f)        { this.fertiliser = f; }
    public String getObservations()            { return observations; }
    public void setObservations(String o)      { this.observations = o; }
    public String getNotes()                   { return notes; }
    public void setNotes(String n)             { this.notes = n; }
    public String getSmartTip()                { return smartTip; }
    public void setSmartTip(String t)          { this.smartTip = t; }
    public LocalDateTime getLoggedAt()         { return loggedAt; }
    public void setLoggedAt(LocalDateTime t)   { this.loggedAt = t; }
}
