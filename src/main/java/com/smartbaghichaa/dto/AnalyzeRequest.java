package com.smartbaghichaa.dto;

public class AnalyzeRequest {

    private String base64Image;
    private String plantName;
    private int daysSincePlanted;
    private String userSelectedStage;
    private Integer userHealthScore;   // form health selector value (0-100)
    private String userCity;           // user's city for location-aware AI tips

    public AnalyzeRequest() {}

    public String getBase64Image()              { return base64Image; }
    public void setBase64Image(String v)        { this.base64Image = v; }
    public String getPlantName()                { return plantName; }
    public void setPlantName(String v)          { this.plantName = v; }
    public int getDaysSincePlanted()            { return daysSincePlanted; }
    public void setDaysSincePlanted(int v)      { this.daysSincePlanted = v; }
    public String getUserSelectedStage()        { return userSelectedStage; }
    public void setUserSelectedStage(String v)  { this.userSelectedStage = v; }
    public Integer getUserHealthScore()         { return userHealthScore; }
    public void setUserHealthScore(Integer v)   { this.userHealthScore = v; }
    public String getUserCity()                 { return userCity; }
    public void setUserCity(String v)           { this.userCity = v; }
}
