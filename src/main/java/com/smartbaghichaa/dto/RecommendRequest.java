package com.smartbaghichaa.dto;

import java.util.List;

public class RecommendRequest {
    private String city;
    private String season;
    private String space;
    private String experience;
    private List<String> preferences;

    public String getCity()                        { return city; }
    public void setCity(String city)               { this.city = city; }
    public String getSeason()                      { return season; }
    public void setSeason(String season)           { this.season = season; }
    public String getSpace()                       { return space; }
    public void setSpace(String space)             { this.space = space; }
    public String getExperience()                  { return experience; }
    public void setExperience(String experience)   { this.experience = experience; }
    public List<String> getPreferences()           { return preferences; }
    public void setPreferences(List<String> p)     { this.preferences = p; }
}
