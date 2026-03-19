package com.smartbaghichaa.service;

import com.smartbaghichaa.dto.LogGrowthRequest;
import com.smartbaghichaa.entity.GrowthLog;
import com.smartbaghichaa.entity.User;
import com.smartbaghichaa.entity.UserPlant;
import com.smartbaghichaa.repository.GrowthLogRepository;
import com.smartbaghichaa.repository.UserPlantRepository;
import com.smartbaghichaa.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TrackerService {

    @Autowired private GrowthLogRepository growthLogRepository;
    @Autowired private UserPlantRepository userPlantRepository;
    @Autowired private UserRepository userRepository;

    // ── LOG GROWTH ──────────────────────────────────────────────────────────
    public Map<String, Object> logGrowth(String email, LogGrowthRequest req) {
        User user = findUser(email);

        UserPlant userPlant = userPlantRepository.findByIdAndUserId(req.getPlantId(), user.getId())
            .orElseThrow(() -> new IllegalArgumentException("Plant not found in your garden"));

        // Calculate days since added
        long daysNum = 0;
        if (userPlant.getAddedAt() != null) {
            daysNum = Math.max(1, ChronoUnit.DAYS.between(userPlant.getAddedAt().toLocalDate(), LocalDate.now()));
        }

        // Build observations string
        String obsStr = req.getObservations() != null
            ? String.join(",", req.getObservations()) : "";

        // Generate smart tip
        String smartTip = buildSmartTip(
            req.getStage() == null ? "" : req.getStage(),
            req.getObservations() == null ? Collections.emptyList() : req.getObservations(),
            req.getHealthScore() == null ? 70 : req.getHealthScore(),
            req.getSoilCondition(),
            req.getStemCondition(),
            req.getLeafColour(),
            req.getSunlight(),
            req.getFertiliser(),
            userPlant.getPlantName(),
            daysNum
        );

        GrowthLog log = new GrowthLog();
        log.setUserPlantId(req.getPlantId());
        log.setStage(req.getStage());
        log.setHealthScore(req.getHealthScore());
        log.setWatering(req.getWatering());
        log.setSunlight(req.getSunlight());
        log.setSoilCondition(req.getSoilCondition());
        log.setLeafColour(req.getLeafColour());
        log.setStemCondition(req.getStemCondition());
        log.setObservations(obsStr);
        log.setFertiliser(req.getFertiliser());
        log.setNotes(req.getNotes());
        log.setSmartTip(smartTip);

        GrowthLog saved = growthLogRepository.save(log);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id",            saved.getId());
        result.put("plantId",       saved.getUserPlantId());
        result.put("plantName",     userPlant.getPlantName());
        result.put("plantEmoji",    userPlant.getPlantEmoji());
        result.put("stage",         saved.getStage());
        result.put("healthScore",   saved.getHealthScore());
        result.put("watering",      saved.getWatering());
        result.put("sunlight",      saved.getSunlight());
        result.put("soilCondition", saved.getSoilCondition());
        result.put("leafColour",    saved.getLeafColour());
        result.put("stemCondition", saved.getStemCondition());
        result.put("observations",  req.getObservations());
        result.put("fertiliser",    saved.getFertiliser());
        result.put("notes",         saved.getNotes());
        result.put("smartTip",      smartTip);
        result.put("daysNum",       daysNum);
        result.put("loggedAt",      saved.getLoggedAt());
        return result;
    }

    // ── ALL LOGS ─────────────────────────────────────────────────────────────
    public List<Map<String, Object>> getAllLogs(String email) {
        User user = findUser(email);
        List<UserPlant> plants = userPlantRepository.findByUserId(user.getId());
        if (plants.isEmpty()) return Collections.emptyList();

        Map<Long, UserPlant> plantMap = plants.stream()
            .collect(Collectors.toMap(UserPlant::getId, p -> p));

        List<Long> plantIds = plants.stream().map(UserPlant::getId).collect(Collectors.toList());
        List<GrowthLog> logs = growthLogRepository.findByUserPlantIdInOrderByLoggedAtDesc(plantIds);

        return logs.stream().map(log -> toLogMap(log, plantMap)).collect(Collectors.toList());
    }

    // ── LOGS FOR ONE PLANT ──────────────────────────────────────────────────
    public List<Map<String, Object>> getLogsForPlant(String email, Long plantId) {
        User user = findUser(email);
        UserPlant userPlant = userPlantRepository.findByIdAndUserId(plantId, user.getId())
            .orElseThrow(() -> new IllegalArgumentException("Plant not found"));

        List<GrowthLog> logs = growthLogRepository.findByUserPlantIdOrderByLoggedAtDesc(plantId);
        Map<Long, UserPlant> plantMap = Map.of(plantId, userPlant);
        return logs.stream().map(log -> toLogMap(log, plantMap)).collect(Collectors.toList());
    }

    // ── STREAK ───────────────────────────────────────────────────────────────
    public Map<String, Object> getStreak(String email) {
        User user = findUser(email);
        List<UserPlant> plants = userPlantRepository.findByUserId(user.getId());
        if (plants.isEmpty()) return Map.of("streak", 0, "message", "No plants added yet");

        List<Long> ids = plants.stream().map(UserPlant::getId).collect(Collectors.toList());
        List<GrowthLog> logs = growthLogRepository.findByUserPlantIdInOrderByLoggedAtDesc(ids);

        Set<LocalDate> logDates = logs.stream()
            .filter(l -> l.getLoggedAt() != null)
            .map(l -> l.getLoggedAt().toLocalDate())
            .collect(Collectors.toSet());

        int streak = 0;
        LocalDate day = LocalDate.now();
        while (logDates.contains(day)) {
            streak++;
            day = day.minusDays(1);
        }

        String msg = streak == 0 ? "Log today to start your streak!"
                   : streak == 1 ? "Great start! Log tomorrow to build your streak."
                   : "Amazing! " + streak + "-day streak! Keep it going!";

        return Map.of("streak", streak, "message", msg);
    }

    // ── PRIVATE HELPERS ──────────────────────────────────────────────────────

    private Map<String, Object> toLogMap(GrowthLog log, Map<Long, UserPlant> plantMap) {
        UserPlant up = plantMap.get(log.getUserPlantId());
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",            log.getId());
        m.put("plantId",       log.getUserPlantId());
        m.put("plantName",     up != null ? up.getPlantName() : "");
        m.put("plantEmoji",    up != null ? up.getPlantEmoji() : "");
        m.put("stage",         log.getStage());
        m.put("healthScore",   log.getHealthScore());
        m.put("watering",      log.getWatering());
        m.put("sunlight",      log.getSunlight());
        m.put("soilCondition", log.getSoilCondition());
        m.put("leafColour",    log.getLeafColour());
        m.put("stemCondition", log.getStemCondition());
        m.put("observations",  log.getObservations() == null ? Collections.emptyList()
                               : Arrays.asList(log.getObservations().split(",")));
        m.put("fertiliser",    log.getFertiliser());
        m.put("notes",         log.getNotes());
        m.put("smartTip",      log.getSmartTip());
        m.put("loggedAt",      log.getLoggedAt());
        return m;
    }

    // ── SMART TIP ENGINE (mirrors _buildSmartTip from HTML) ─────────────────
    private String buildSmartTip(String stage, List<String> obs, int healthVal,
                                  String soil, String stem, String leaf, String sun,
                                  String fert, String plant, long days) {
        if ("waterlogged".equals(soil))
            return "Root rot risk! Move to fresh well-draining soil immediately. Waterlogged roots suffocate fast.";

        if ("soft".equals(stem) && healthVal < 50)
            return "Soft wilting + low health = overwatering or root rot. Let soil dry completely before next watering.";

        if (obs.stream().anyMatch(o -> o.contains("Root bound")))
            return "Time to repot! Choose a pot 2 inches wider. Spring is the best time.";

        if (obs.stream().anyMatch(o -> o.contains("Pests")))
            return "Spray neem oil (5ml+1L water+2 drops dish soap) every 3 days for 2 weeks. Check leaf undersides.";

        if ("yellow".equals(leaf) || obs.stream().anyMatch(o -> o.contains("Yellow"))) {
            if ("wet".equals(soil) || "waterlogged".equals(soil))
                return "Yellow leaves + wet soil = overwatering. Let soil dry 2 inches deep before next watering.";
            return "Yellow leaves: check for overwatering, low nitrogen, or insufficient light. Try moving to a brighter spot first.";
        }

        if ("brown_tips".equals(leaf))
            return "Brown tips = underwatering or low humidity. Water more consistently and mist leaves in dry weather.";

        if ("leggy".equals(stem))
            return "Leggy growth = insufficient light. Move 1-2 feet closer to window or to a south-facing spot.";

        if ("very_dry".equals(soil) || "dry".equals(soil))
            return "Soil is dry — water thoroughly until it drains from the bottom, then let top inch dry before next watering.";

        if ("none".equals(sun) && stage.contains("Flower"))
            return "Flowering plants need 6+ hours of sun. No sun today may delay fruit set — move to a brighter location.";

        Map<String, String> stageMap = new LinkedHashMap<>();
        stageMap.put("Just Planted", "Water gently and keep in indirect light for 7 days. No fertiliser for the first 2 weeks.");
        stageMap.put("Sprouting",    "Most delicate stage! Keep soil evenly moist, protect from wind and harsh direct sun.");
        stageMap.put("Growing",      "Active growth! Start diluted liquid fertiliser every 10 days. Ensure 4-6 hours of sunlight daily.");
        stageMap.put("Leafy",        "Pinch growing tips for bushier shape. Check if roots peek out of drainage holes.");
        stageMap.put("Full",         "Pinch growing tips for bushier shape. Check if roots peek out of drainage holes — repot if so.");
        stageMap.put("Flowering",    "Switch to potassium feed (banana peel water works!). Reduce nitrogen. Hand-pollinate if indoors.");
        stageMap.put("Harvest",      "Harvest in the morning when flavour is best. Pick regularly to encourage more fruiting.");

        String key = stageMap.keySet().stream().filter(stage::contains).findFirst().orElse(null);
        if (key != null) return stageMap.get(key);

        if (healthVal <= 40)
            return "Health is low for " + plant + ". Check: correct sunlight? Right watering schedule? Any pests on leaf undersides?";

        if (days > 30 && healthVal >= 70)
            return "Excellent! " + plant + " is thriving after " + days + " days. Keep up the consistent care routine!";

        return "";
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
