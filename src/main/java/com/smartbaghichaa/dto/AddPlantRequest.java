package com.smartbaghichaa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AddPlantRequest {

    @NotBlank(message = "Plant name is required")
    @Size(max = 100, message = "Plant name must be under 100 characters")
    private String plantName;

    @Size(max = 10, message = "Emoji must be under 10 characters")
    private String plantEmoji;

    @Size(max = 60, message = "Category must be under 60 characters")
    private String category;

    public String getPlantName()              { return plantName; }
    public void setPlantName(String n)        { this.plantName = n; }
    public String getPlantEmoji()             { return plantEmoji; }
    public void setPlantEmoji(String e)       { this.plantEmoji = e; }
    public String getCategory()               { return category; }
    public void setCategory(String c)         { this.category = c; }
}
