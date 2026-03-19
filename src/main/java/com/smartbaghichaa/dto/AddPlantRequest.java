package com.smartbaghichaa.dto;

public class AddPlantRequest {
    private String plantName;
    private String plantEmoji;
    private String category;

    public String getPlantName()              { return plantName; }
    public void setPlantName(String n)        { this.plantName = n; }
    public String getPlantEmoji()             { return plantEmoji; }
    public void setPlantEmoji(String e)       { this.plantEmoji = e; }
    public String getCategory()               { return category; }
    public void setCategory(String c)         { this.category = c; }
}
