package com.smartbaghichaa.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class LogGrowthRequest {

    @NotNull(message = "Plant ID is required")
    private Long plantId;       // user_plants.id

    private String stage;

    @Min(value = 0, message = "Health score must be 0–100")
    @Max(value = 100, message = "Health score must be 0–100")
    private Integer healthScore;

    private String watering;
    private String sunlight;
    private String soilCondition;
    private String leafColour;
    private String stemCondition;
    private List<String> observations;
    private String fertiliser;
    private String notes;
    private Boolean aiAnalyzed;

    @Min(value = 0, message = "AI health score must be 0–100")
    @Max(value = 100, message = "AI health score must be 0–100")
    private Integer aiHealthScore;

    private String city;  // ISSUE-08: optional city for weather-aware smart tips

    public Long getPlantId()                        { return plantId; }
    public void setPlantId(Long p)                  { this.plantId = p; }
    public String getStage()                        { return stage; }
    public void setStage(String s)                  { this.stage = s; }
    public Integer getHealthScore()                 { return healthScore; }
    public void setHealthScore(Integer h)           { this.healthScore = h; }
    public String getWatering()                     { return watering; }
    public void setWatering(String w)               { this.watering = w; }
    public String getSunlight()                     { return sunlight; }
    public void setSunlight(String s)               { this.sunlight = s; }
    public String getSoilCondition()                { return soilCondition; }
    public void setSoilCondition(String s)          { this.soilCondition = s; }
    public String getLeafColour()                   { return leafColour; }
    public void setLeafColour(String l)             { this.leafColour = l; }
    public String getStemCondition()                { return stemCondition; }
    public void setStemCondition(String s)          { this.stemCondition = s; }
    public List<String> getObservations()           { return observations; }
    public void setObservations(List<String> o)     { this.observations = o; }
    public String getFertiliser()                   { return fertiliser; }
    public void setFertiliser(String f)             { this.fertiliser = f; }
    public String getNotes()                        { return notes; }
    public void setNotes(String n)                  { this.notes = n; }
    public Boolean getAiAnalyzed()                  { return aiAnalyzed; }
    public void setAiAnalyzed(Boolean a)            { this.aiAnalyzed = a; }
    public Integer getAiHealthScore()               { return aiHealthScore; }
    public void setAiHealthScore(Integer s)         { this.aiHealthScore = s; }
    public String getCity()                         { return city; }
    public void setCity(String c)                   { this.city = c; }
}
