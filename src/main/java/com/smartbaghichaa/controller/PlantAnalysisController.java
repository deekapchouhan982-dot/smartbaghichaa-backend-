package com.smartbaghichaa.controller;

import com.smartbaghichaa.dto.AnalyzeRequest;
import com.smartbaghichaa.dto.PlantAnalysisResult;
import com.smartbaghichaa.entity.PlantAnalysis;
import com.smartbaghichaa.repository.PlantAnalysisRepository;
import com.smartbaghichaa.security.JwtUtil;
import com.smartbaghichaa.service.GeminiVisionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analyze")
public class PlantAnalysisController {

    @Autowired private GeminiVisionService geminiService;
    @Autowired private PlantAnalysisRepository analysisRepo;
    @Autowired private JwtUtil jwtUtil;

    // ── POST /api/analyze/photo ───────────────────────────────────────────────
    @PostMapping("/photo")
    public ResponseEntity<?> analyzePhoto(
            @RequestHeader("Authorization") String auth,
            @RequestBody AnalyzeRequest request) {

        String email = extractEmail(auth);
        if (email == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        // ── Daily rate limit: max 10 AI analyses per user per day ─────────────
        java.time.LocalDateTime startOfDay = java.time.LocalDate.now().atStartOfDay();
        long todayCount = analysisRepo.countByUserEmailAndAnalyzedAtAfter(email, startOfDay);
        if (todayCount >= 10) {
            return ResponseEntity.status(429).body(Map.of(
                "error",   "daily_limit_reached",
                "message", "You've used all 10 AI analyses for today. Please try again tomorrow.",
                "count",   todayCount
            ));
        }

        // ── Image size guard: reject > 1.5 MB base64 (~1.1 MB raw) ───────────
        String b64 = request.getBase64Image();
        if (b64 != null && b64.length() > 2_000_000) {
            return ResponseEntity.status(413).body(Map.of(
                "error",   "image_too_large",
                "message", "Photo is too large. Please use a photo under 1.5 MB or let the app compress it."
            ));
        }

        // ── Plant name guard: reject blank or placeholder ─────────────────────
        String plantName = request.getPlantName();
        if (plantName == null || plantName.isBlank()
                || plantName.toLowerCase().contains("select")
                || plantName.equals("—")) {
            return ResponseEntity.status(400).body(Map.of(
                "error",   "invalid_plant",
                "message", "Please select a valid plant before analyzing."
            ));
        }

        PlantAnalysisResult result = geminiService.analyzePhoto(
            b64,
            plantName,
            request.getDaysSincePlanted(),
            request.getUserSelectedStage(),
            request.getUserHealthScore(),
            request.getUserCity()
        );

        // Reject non-plant images — don't save to DB
        if (!result.isIsPlant()) {
            return ResponseEntity.status(422).body(Map.of(
                "error",    "not_a_plant",
                "message",  "This doesn't look like a plant photo. " + result.getNotPlantReason()
                            + " Please upload a clear photo of your plant.",
                "isPlant",  false
            ));
        }

        // Save analysis to DB
        PlantAnalysis analysis = new PlantAnalysis();
        analysis.setUserEmail(email);
        analysis.setPlantName(plantName);
        analysis.setHealthScore(result.getHealthScore());
        analysis.setDetectedStage(result.getDetectedStage());
        analysis.setLeafCondition(result.getLeafCondition());
        analysis.setSoilCondition(result.getSoilCondition());
        analysis.setStemCondition(result.getStemCondition());
        analysis.setPestDetected(result.isPestDetected());
        analysis.setPestDetails(result.getPestDetails());
        analysis.setDiseaseDetected(result.isDiseaseDetected());
        analysis.setDiseaseDetails(result.getDiseaseDetails());
        analysis.setGrowthStatus(result.getGrowthStatus());
        analysis.setGrowthPercent(result.getGrowthPercent());
        analysis.setImmediateAction(result.getImmediateAction());
        analysis.setTodayTip(result.getTodayTip());
        analysis.setWeeklyPlan(result.getWeeklyPlan());
        analysis.setWateringFrequency(result.getWateringFrequency());
        analysis.setNeedsFertiliser(result.isNeedsFertiliser());
        analysis.setFertiliserIn(result.getFertiliserIn());
        analysis.setDaysToHarvest(result.getDaysToHarvest());
        analysis.setConfidenceScore(result.getConfidenceScore());
        // ── Save new expert fields ──
        analysis.setExpertVerdict(result.getExpertVerdict());
        analysis.setRootCause(result.getRootCause());
        analysis.setTreatmentSteps(result.getTreatmentSteps());
        analysis.setCareSchedule(result.getCareSchedule());
        analysis.setLeafAnalysis(result.getLeafAnalysis());
        analysis.setSoilFix(result.getSoilFix());
        analysis.setAnalyzedAt(LocalDateTime.now());
        analysisRepo.save(analysis);

        // Build alert message for AI-powered reminders
        String alertMsg = buildAIAlert(request.getPlantName(), result);

        // ── Determine AI status message ──────────────────────────────────────
        // If a photo was uploaded but AI didn't power the result, the service
        // hit a quota/network error — tell the user clearly instead of showing
        // the misleading "Upload a photo" message.
        boolean photoProvided = b64 != null && !b64.isBlank();
        String aiStatusMessage = null;
        if (!result.isAiPowered() && photoProvided) {
            aiStatusMessage = "AI analysis is temporarily unavailable (API quota reached). Showing rule-based estimates from your inputs.";
        }

        Map<String, Object> response = new HashMap<>();
        response.put("healthScore",       result.getHealthScore());
        response.put("detectedStage",     result.getDetectedStage());
        response.put("leafCondition",     result.getLeafCondition());
        response.put("soilCondition",     result.getSoilCondition());
        response.put("stemCondition",     result.getStemCondition());
        response.put("pestDetected",      result.isPestDetected());
        response.put("pestDetails",       result.getPestDetails() != null ? result.getPestDetails() : "");
        response.put("diseaseDetected",   result.isDiseaseDetected());
        response.put("diseaseDetails",    result.getDiseaseDetails() != null ? result.getDiseaseDetails() : "");
        response.put("growthStatus",      result.getGrowthStatus());
        response.put("growthPercent",     result.getGrowthPercent());
        response.put("immediateAction",   result.getImmediateAction());
        response.put("todayTip",          result.getTodayTip());
        response.put("weeklyPlan",        result.getWeeklyPlan());
        response.put("wateringFrequency", result.getWateringFrequency());
        response.put("needsFertiliser",   result.isNeedsFertiliser());
        response.put("fertiliserIn",      result.getFertiliserIn());
        response.put("daysToHarvest",     result.getDaysToHarvest());
        response.put("confidenceScore",   result.getConfidenceScore());
        response.put("stageConfirmed",    result.isStageConfirmed());
        response.put("correctedStage",    result.getCorrectedStage() != null ? result.getCorrectedStage() : "");
        response.put("alertMessage",      alertMsg);
        response.put("aiPowered",         result.isAiPowered());
        response.put("aiStatusMessage",   aiStatusMessage != null ? aiStatusMessage : "");
        // ── Expert fields ──
        response.put("expertVerdict",     result.getExpertVerdict() != null ? result.getExpertVerdict() : "");
        response.put("rootCause",         result.getRootCause() != null ? result.getRootCause() : "");
        response.put("treatmentSteps",    result.getTreatmentSteps() != null ? result.getTreatmentSteps() : "");
        response.put("careSchedule",      result.getCareSchedule() != null ? result.getCareSchedule() : "");
        response.put("leafAnalysis",      result.getLeafAnalysis() != null ? result.getLeafAnalysis() : "");
        response.put("soilFix",           result.getSoilFix() != null ? result.getSoilFix() : "");
        return ResponseEntity.ok(response);
    }

    // ── GET /api/analyze/history/{plantName} ─────────────────────────────────
    @GetMapping("/history/{plantName}")
    public ResponseEntity<?> getAnalysisHistory(
            @RequestHeader("Authorization") String auth,
            @PathVariable String plantName) {

        String email = extractEmail(auth);
        if (email == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        List<PlantAnalysis> history =
            analysisRepo.findByUserEmailAndPlantNameOrderByAnalyzedAtDesc(email, plantName);
        return ResponseEntity.ok(Map.of("analyses", history));
    }

    // ── GET /api/analyze/timeline/{plantName} ────────────────────────────────
    @GetMapping("/timeline/{plantName}")
    public ResponseEntity<?> getTimeline(
            @RequestHeader("Authorization") String auth,
            @PathVariable String plantName) {

        String email = extractEmail(auth);
        if (email == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        List<PlantAnalysis> analyses =
            analysisRepo.findByUserEmailAndPlantNameOrderByAnalyzedAtDesc(email, plantName);
        return ResponseEntity.ok(Map.of("timeline", analyses));
    }

    // ── GET /api/analyze/trend/{plantName} ───────────────────────────────────
    @GetMapping("/trend/{plantName}")
    public ResponseEntity<?> getHealthTrend(
            @RequestHeader("Authorization") String auth,
            @PathVariable String plantName) {

        String email = extractEmail(auth);
        if (email == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        List<PlantAnalysis> analyses =
            analysisRepo.findByUserEmailAndPlantNameOrderByAnalyzedAtAsc(email, plantName);

        List<Map<String, Object>> trend = analyses.stream().map(a -> {
            Map<String, Object> point = new HashMap<>();
            point.put("date",            a.getAnalyzedAt() != null ? a.getAnalyzedAt().toLocalDate().toString() : "");
            point.put("healthScore",     a.getHealthScore());
            point.put("growthStatus",    a.getGrowthStatus());
            point.put("pestDetected",    Boolean.TRUE.equals(a.getPestDetected()));
            point.put("diseaseDetected", Boolean.TRUE.equals(a.getDiseaseDetected()));
            return point;
        }).collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(Map.of("trend", trend, "plant", plantName));
    }

    // ── GET /api/analyze/summary ─────────────────────────────────────────────
    @GetMapping("/summary")
    public ResponseEntity<?> getAISummary(@RequestHeader("Authorization") String auth) {
        String email = extractEmail(auth);
        if (email == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        List<PlantAnalysis> all = analysisRepo.findByUserEmailOrderByAnalyzedAtDesc(email);

        // Return latest analysis per plant
        Map<String, PlantAnalysis> latestPerPlant = new java.util.LinkedHashMap<>();
        for (PlantAnalysis a : all) {
            latestPerPlant.putIfAbsent(a.getPlantName(), a);
        }
        return ResponseEntity.ok(Map.of("summary", latestPerPlant.values()));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private String buildAIAlert(String plantName, PlantAnalysisResult result) {
        if (result.isPestDetected()) {
            return "Pests detected on your " + plantName + ". Spray neem oil today!";
        }
        if ("Very Dry".equals(result.getSoilCondition())) {
            return plantName + " needs water NOW — AI detected very dry soil.";
        }
        if ("behind".equals(result.getGrowthStatus()) || "concern".equals(result.getGrowthStatus())) {
            return "Your " + plantName + " needs attention. Check the AI tip in tracker.";
        }
        if (result.isNeedsFertiliser()) {
            return "Time to fertilise your " + plantName + "! AI recommends doing it " + result.getFertiliserIn() + ".";
        }
        return null;
    }

    private String extractEmail(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        String token = authHeader.substring(7);
        if (!jwtUtil.isTokenValid(token)) return null;
        return jwtUtil.extractEmail(token);
    }
}
