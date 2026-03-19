package com.smartbaghichaa.dto;

import java.util.List;

public class LogGrowthRequest {
    private Long plantId;       // user_plants.id
    private String stage;
    private Integer healthScore;
    private String watering;
    private String sunlight;
    private String soilCondition;
    private String leafColour;
    private String stemCondition;
    private List<String> observations;
    private String fertiliser;
    private String notes;

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
}
