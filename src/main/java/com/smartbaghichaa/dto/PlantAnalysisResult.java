package com.smartbaghichaa.dto;

public class PlantAnalysisResult {

    private Integer healthScore;
    private String detectedStage;
    private String leafCondition;
    private String soilCondition;
    private String stemCondition;
    private boolean pestDetected;
    private String pestDetails;
    private boolean diseaseDetected;
    private String diseaseDetails;
    private String growthStatus;
    private Integer growthPercent;
    private Integer daysToHarvest;
    private String immediateAction;
    private String todayTip;
    private String weeklyPlan;
    private String wateringFrequency;
    private boolean needsFertiliser;
    private String fertiliserIn;
    private Double confidenceScore;
    private boolean aiPowered;
    private boolean isPlant = true;
    private String notPlantReason;
    private boolean stageConfirmed = true;
    private String correctedStage;

    // ── Expert-level extended fields ──────────────────────────────────────────
    private String expertVerdict;      // one-liner diagnosis like a doctor
    private String rootCause;          // root cause of health issues
    private String treatmentSteps;     // "|"-separated numbered steps
    private String careSchedule;       // "Mon:action|Tue:action|…" 7-day plan
    private String leafAnalysis;       // plain-language explanation of leaf condition
    private String soilFix;            // what to do about soil right now

    public PlantAnalysisResult() {}

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
    public boolean isPestDetected()              { return pestDetected; }
    public void setPestDetected(boolean v)       { this.pestDetected = v; }
    public String getPestDetails()               { return pestDetails; }
    public void setPestDetails(String v)         { this.pestDetails = v; }
    public boolean isDiseaseDetected()           { return diseaseDetected; }
    public void setDiseaseDetected(boolean v)    { this.diseaseDetected = v; }
    public String getDiseaseDetails()            { return diseaseDetails; }
    public void setDiseaseDetails(String v)      { this.diseaseDetails = v; }
    public String getGrowthStatus()              { return growthStatus; }
    public void setGrowthStatus(String v)        { this.growthStatus = v; }
    public Integer getGrowthPercent()            { return growthPercent; }
    public void setGrowthPercent(Integer v)      { this.growthPercent = v; }
    public Integer getDaysToHarvest()            { return daysToHarvest; }
    public void setDaysToHarvest(Integer v)      { this.daysToHarvest = v; }
    public String getImmediateAction()           { return immediateAction; }
    public void setImmediateAction(String v)     { this.immediateAction = v; }
    public String getTodayTip()                  { return todayTip; }
    public void setTodayTip(String v)            { this.todayTip = v; }
    public String getWeeklyPlan()                { return weeklyPlan; }
    public void setWeeklyPlan(String v)          { this.weeklyPlan = v; }
    public String getWateringFrequency()         { return wateringFrequency; }
    public void setWateringFrequency(String v)   { this.wateringFrequency = v; }
    public boolean isNeedsFertiliser()           { return needsFertiliser; }
    public void setNeedsFertiliser(boolean v)    { this.needsFertiliser = v; }
    public String getFertiliserIn()              { return fertiliserIn; }
    public void setFertiliserIn(String v)        { this.fertiliserIn = v; }
    public Double getConfidenceScore()           { return confidenceScore; }
    public void setConfidenceScore(Double v)     { this.confidenceScore = v; }
    public boolean isAiPowered()                 { return aiPowered; }
    public void setAiPowered(boolean v)          { this.aiPowered = v; }
    public boolean isIsPlant()                   { return isPlant; }
    public void setIsPlant(boolean v)            { this.isPlant = v; }
    public String getNotPlantReason()            { return notPlantReason; }
    public void setNotPlantReason(String v)      { this.notPlantReason = v; }
    public boolean isStageConfirmed()            { return stageConfirmed; }
    public void setStageConfirmed(boolean v)     { this.stageConfirmed = v; }
    public String getCorrectedStage()            { return correctedStage; }
    public void setCorrectedStage(String v)      { this.correctedStage = v; }

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
}
