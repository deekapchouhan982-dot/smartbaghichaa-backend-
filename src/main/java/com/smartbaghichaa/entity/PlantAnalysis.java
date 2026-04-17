package com.smartbaghichaa.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "plant_analyses")
public class PlantAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userPlantId;
    private String userEmail;
    private String plantName;

    // AI Results
    private Integer healthScore;
    private String detectedStage;
    private String leafCondition;
    private String soilCondition;
    private String stemCondition;
    private Boolean pestDetected;
    private String pestDetails;
    private Boolean diseaseDetected;
    private String diseaseDetails;

    // Growth Tracking
    private String expectedStage;
    private Boolean onTrack;
    private Integer growthPercent;
    private Integer daysToHarvest;
    private String growthStatus;

    // AI Recommendations
    @Column(columnDefinition = "TEXT")
    private String immediateAction;
    @Column(columnDefinition = "TEXT")
    private String todayTip;
    @Column(columnDefinition = "TEXT")
    private String weeklyPlan;

    // Reminder adjustments
    private String wateringFrequency;
    private String nextWateringIn;
    private Boolean needsFertiliser;
    private String fertiliserIn;

    // Expert AI extended fields (added Day 4)
    @Column(columnDefinition = "TEXT")
    private String expertVerdict;
    @Column(columnDefinition = "TEXT")
    private String rootCause;
    @Column(columnDefinition = "TEXT")
    private String treatmentSteps;
    @Column(columnDefinition = "TEXT")
    private String careSchedule;
    @Column(columnDefinition = "TEXT")
    private String leafAnalysis;
    @Column(columnDefinition = "TEXT")
    private String soilFix;

    // Photo + raw JSON
    @Column(columnDefinition = "TEXT")
    private String photoUrl;
    @Column(columnDefinition = "TEXT")
    private String analysisJson;

    private LocalDateTime analyzedAt;
    private Double confidenceScore;

    public PlantAnalysis() {}

    public Long getId()                          { return id; }
    public void setId(Long id)                   { this.id = id; }
    public Long getUserPlantId()                 { return userPlantId; }
    public void setUserPlantId(Long v)           { this.userPlantId = v; }
    public String getUserEmail()                 { return userEmail; }
    public void setUserEmail(String v)           { this.userEmail = v; }
    public String getPlantName()                 { return plantName; }
    public void setPlantName(String v)           { this.plantName = v; }
    public Integer getHealthScore()              { return healthScore; }
    public void setHealthScore(Integer v)        { this.healthScore = v; }
    public String getDetectedStage()             { return detectedStage; }
    public void setDetectedStage(String v)       { this.detectedStage = v; }
    public String getLeafCondition()             { return leafCondition; }
    public void setLeafCondition(String v)       { this.leafCondition = v; }
    public String getSoilCondition()             { return soilCondition; }
    public void setSoilCondition(String v)       { this.soilCondition = v; }
    public String getStemCondition()             { return stemCondition; }
    public void setStemCondition(String v)       { this.stemCondition = v; }
    public Boolean getPestDetected()             { return pestDetected; }
    public void setPestDetected(Boolean v)       { this.pestDetected = v; }
    public String getPestDetails()               { return pestDetails; }
    public void setPestDetails(String v)         { this.pestDetails = v; }
    public Boolean getDiseaseDetected()          { return diseaseDetected; }
    public void setDiseaseDetected(Boolean v)    { this.diseaseDetected = v; }
    public String getDiseaseDetails()            { return diseaseDetails; }
    public void setDiseaseDetails(String v)      { this.diseaseDetails = v; }
    public String getExpectedStage()             { return expectedStage; }
    public void setExpectedStage(String v)       { this.expectedStage = v; }
    public Boolean getOnTrack()                  { return onTrack; }
    public void setOnTrack(Boolean v)            { this.onTrack = v; }
    public Integer getGrowthPercent()            { return growthPercent; }
    public void setGrowthPercent(Integer v)      { this.growthPercent = v; }
    public Integer getDaysToHarvest()            { return daysToHarvest; }
    public void setDaysToHarvest(Integer v)      { this.daysToHarvest = v; }
    public String getGrowthStatus()              { return growthStatus; }
    public void setGrowthStatus(String v)        { this.growthStatus = v; }
    public String getImmediateAction()           { return immediateAction; }
    public void setImmediateAction(String v)     { this.immediateAction = v; }
    public String getTodayTip()                  { return todayTip; }
    public void setTodayTip(String v)            { this.todayTip = v; }
    public String getWeeklyPlan()                { return weeklyPlan; }
    public void setWeeklyPlan(String v)          { this.weeklyPlan = v; }
    public String getWateringFrequency()         { return wateringFrequency; }
    public void setWateringFrequency(String v)   { this.wateringFrequency = v; }
    public String getNextWateringIn()            { return nextWateringIn; }
    public void setNextWateringIn(String v)      { this.nextWateringIn = v; }
    public Boolean getNeedsFertiliser()          { return needsFertiliser; }
    public void setNeedsFertiliser(Boolean v)    { this.needsFertiliser = v; }
    public String getFertiliserIn()              { return fertiliserIn; }
    public void setFertiliserIn(String v)        { this.fertiliserIn = v; }
    public String getExpertVerdict()             { return expertVerdict; }
    public void setExpertVerdict(String v)       { this.expertVerdict = v; }
    public String getRootCause()                 { return rootCause; }
    public void setRootCause(String v)           { this.rootCause = v; }
    public String getTreatmentSteps()            { return treatmentSteps; }
    public void setTreatmentSteps(String v)      { this.treatmentSteps = v; }
    public String getCareSchedule()              { return careSchedule; }
    public void setCareSchedule(String v)        { this.careSchedule = v; }
    public String getLeafAnalysis()              { return leafAnalysis; }
    public void setLeafAnalysis(String v)        { this.leafAnalysis = v; }
    public String getSoilFix()                   { return soilFix; }
    public void setSoilFix(String v)             { this.soilFix = v; }
    public String getPhotoUrl()                  { return photoUrl; }
    public void setPhotoUrl(String v)            { this.photoUrl = v; }
    public String getAnalysisJson()              { return analysisJson; }
    public void setAnalysisJson(String v)        { this.analysisJson = v; }
    public LocalDateTime getAnalyzedAt()         { return analyzedAt; }
    public void setAnalyzedAt(LocalDateTime v)   { this.analyzedAt = v; }
    public Double getConfidenceScore()           { return confidenceScore; }
    public void setConfidenceScore(Double v)     { this.confidenceScore = v; }
}
