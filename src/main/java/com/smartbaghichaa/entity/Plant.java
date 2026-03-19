package com.smartbaghichaa.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "plants")
public class Plant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String emoji;
    private String category;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String water;
    private String sun;

    @Column(name = "care_level")
    private String careLevel;

    @Column(length = 100)
    private String seasons;   // comma-separated e.g. "Su,Mo,Au"

    @Column(length = 200)
    private String climates;  // comma-separated e.g. "hot_dry,humid_sub"

    @Column(length = 100)
    private String spaces;    // comma-separated e.g. "SB,LB,Te"

    @Column(length = 200)
    private String prefs;     // comma-separated e.g. "pHerb,pMed"

    @Column(columnDefinition = "TEXT")
    private String tip;

    public Plant() {}

    public Plant(String name, String emoji, String category, String description,
                 String water, String sun, String careLevel,
                 String seasons, String climates, String spaces, String prefs, String tip) {
        this.name = name; this.emoji = emoji; this.category = category;
        this.description = description; this.water = water; this.sun = sun;
        this.careLevel = careLevel; this.seasons = seasons; this.climates = climates;
        this.spaces = spaces; this.prefs = prefs; this.tip = tip;
    }

    public Long getId()                   { return id; }
    public String getName()               { return name; }
    public void setName(String n)         { this.name = n; }
    public String getEmoji()              { return emoji; }
    public void setEmoji(String e)        { this.emoji = e; }
    public String getCategory()           { return category; }
    public void setCategory(String c)     { this.category = c; }
    public String getDescription()        { return description; }
    public void setDescription(String d)  { this.description = d; }
    public String getWater()              { return water; }
    public void setWater(String w)        { this.water = w; }
    public String getSun()                { return sun; }
    public void setSun(String s)          { this.sun = s; }
    public String getCareLevel()          { return careLevel; }
    public void setCareLevel(String c)    { this.careLevel = c; }
    public String getSeasons()            { return seasons; }
    public void setSeasons(String s)      { this.seasons = s; }
    public String getClimates()           { return climates; }
    public void setClimates(String c)     { this.climates = c; }
    public String getSpaces()             { return spaces; }
    public void setSpaces(String s)       { this.spaces = s; }
    public String getPrefs()              { return prefs; }
    public void setPrefs(String p)        { this.prefs = p; }
    public String getTip()                { return tip; }
    public void setTip(String t)          { this.tip = t; }
}
