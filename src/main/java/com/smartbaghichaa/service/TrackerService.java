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
import org.springframework.web.client.RestTemplate;

@Service
public class TrackerService {

    @Autowired private GrowthLogRepository growthLogRepository;
    @Autowired private UserPlantRepository userPlantRepository;
    @Autowired private UserRepository userRepository;

    // ISSUE-08: Known Indian city coordinates for weather lookup
    private static final Map<String, double[]> CITY_COORDS = new HashMap<>();
    static {
        CITY_COORDS.put("mumbai",     new double[]{19.0760,  72.8777});
        CITY_COORDS.put("delhi",      new double[]{28.6139,  77.2090});
        CITY_COORDS.put("new delhi",  new double[]{28.6139,  77.2090});
        CITY_COORDS.put("bangalore",  new double[]{12.9716,  77.5946});
        CITY_COORDS.put("bengaluru",  new double[]{12.9716,  77.5946});
        CITY_COORDS.put("hyderabad",  new double[]{17.3850,  78.4867});
        CITY_COORDS.put("chennai",    new double[]{13.0827,  80.2707});
        CITY_COORDS.put("kolkata",    new double[]{22.5726,  88.3639});
        CITY_COORDS.put("pune",       new double[]{18.5204,  73.8567});
        CITY_COORDS.put("ahmedabad",  new double[]{23.0225,  72.5714});
        CITY_COORDS.put("jaipur",     new double[]{26.9124,  75.7873});
        CITY_COORDS.put("lucknow",    new double[]{26.8467,  80.9462});
        CITY_COORDS.put("indore",     new double[]{22.7196,  75.8577});
        CITY_COORDS.put("bhopal",     new double[]{23.2599,  77.4126});
        CITY_COORDS.put("noida",      new double[]{28.5355,  77.3910});
        CITY_COORDS.put("gurgaon",    new double[]{28.4595,  77.0266});
        CITY_COORDS.put("gurugram",   new double[]{28.4595,  77.0266});
        CITY_COORDS.put("chandigarh", new double[]{30.7333,  76.7794});
        CITY_COORDS.put("surat",      new double[]{21.1702,  72.8311});
        CITY_COORDS.put("nagpur",     new double[]{21.1458,  79.0882});
        CITY_COORDS.put("patna",      new double[]{25.5941,  85.1376});
        CITY_COORDS.put("vadodara",   new double[]{22.3072,  73.1812});
    }
    private final RestTemplate restTemplate = new RestTemplate();

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

        // ── ISSUE-04: Clamp health score to 0–100, default null to 70 ──
        int clampedScore = req.getHealthScore() != null
            ? Math.max(0, Math.min(100, req.getHealthScore())) : 70;

        // ── Stage validation: warn if stage is too advanced for plant age ──
        String stageWarning = checkStageForDays(req.getStage(), userPlant.getPlantName(), daysNum);

        // Build observations string
        String obsStr = req.getObservations() != null
            ? String.join(",", req.getObservations()) : "";

        // ISSUE-08: Fetch live weather condition for the user's city (optional)
        String weatherCondition = fetchWeatherCondition(req.getCity());

        // Generate smart tip
        String smartTip = buildSmartTip(
            req.getStage() == null ? "" : req.getStage(),
            req.getObservations() == null ? Collections.emptyList() : req.getObservations(),
            clampedScore,
            req.getSoilCondition(),
            req.getStemCondition(),
            req.getLeafColour(),
            req.getSunlight(),
            req.getFertiliser(),
            userPlant.getPlantName(),
            daysNum,
            weatherCondition,
            req.getCity()
        );

        GrowthLog log = new GrowthLog();
        log.setUserPlantId(req.getPlantId());
        log.setStage(req.getStage());
        log.setHealthScore(clampedScore);
        log.setWatering(req.getWatering());
        log.setSunlight(req.getSunlight());
        log.setSoilCondition(req.getSoilCondition());
        log.setLeafColour(req.getLeafColour());
        log.setStemCondition(req.getStemCondition());
        log.setObservations(obsStr);
        log.setFertiliser(req.getFertiliser());
        log.setNotes(escapeHtml(req.getNotes())); // M5: sanitize user text against XSS
        log.setSmartTip(smartTip);
        log.setAiAnalyzed(req.getAiAnalyzed());
        log.setAiHealthScore(req.getAiHealthScore());

        GrowthLog saved = growthLogRepository.save(log);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id",            saved.getId());
        result.put("plantId",       saved.getUserPlantId());
        result.put("plantName",     userPlant.getPlantName());
        result.put("plantEmoji",    userPlant.getPlantEmoji());
        result.put("stage",         saved.getStage());
        result.put("healthScore",   clampedScore);
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
        result.put("aiAnalyzed",    saved.getAiAnalyzed());
        result.put("aiHealthScore", saved.getAiHealthScore());
        result.put("loggedAt",      saved.getLoggedAt());
        if (stageWarning != null) result.put("stageWarning", stageWarning);
        return result;
    }

    // ── ALL LOGS ─────────────────────────────────────────────────────────────
    // limit=0 means fetch ALL logs (used by controller for server-side pagination)
    public List<Map<String, Object>> getAllLogs(String email, int limit) {
        User user = findUser(email);
        List<UserPlant> plants = userPlantRepository.findByUserId(user.getId());
        if (plants.isEmpty()) return Collections.emptyList();

        Map<Long, UserPlant> plantMap = plants.stream()
            .collect(Collectors.toMap(UserPlant::getId, p -> p));

        List<Long> plantIds = plants.stream().map(UserPlant::getId).collect(Collectors.toList());
        List<GrowthLog> logs = growthLogRepository.findByUserPlantIdInOrderByLoggedAtDesc(plantIds);

        // Only pre-limit when using legacy ?limit= param (limit > 0); pagination fetches all (limit=0)
        if (limit > 0 && logs.size() > limit) {
            logs = logs.subList(0, limit);
        }
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

    // ── DELETE LOG ───────────────────────────────────────────────────────────
    public void deleteLog(String email, Long logId) {
        User user = findUser(email);
        GrowthLog log = growthLogRepository.findById(logId)
            .orElseThrow(() -> new IllegalArgumentException("Log not found"));
        // Verify ownership: the log's userPlantId must belong to this user
        userPlantRepository.findByIdAndUserId(log.getUserPlantId(), user.getId())
            .orElseThrow(() -> new IllegalArgumentException("Log not found"));
        growthLogRepository.deleteById(logId);
    }

    // ── EDIT LOG ─────────────────────────────────────────────────────────────
    public Map<String, Object> editLog(String email, Long logId, LogGrowthRequest req) {
        User user = findUser(email);
        GrowthLog log = growthLogRepository.findById(logId)
            .orElseThrow(() -> new IllegalArgumentException("Log not found"));
        UserPlant userPlant = userPlantRepository.findByIdAndUserId(log.getUserPlantId(), user.getId())
            .orElseThrow(() -> new IllegalArgumentException("Log not found"));

        long daysNum = 0;
        if (userPlant.getAddedAt() != null) {
            daysNum = Math.max(1, ChronoUnit.DAYS.between(userPlant.getAddedAt().toLocalDate(), LocalDate.now()));
        }

        // ── ISSUE-04: Clamp health score to 0–100, default null to 70 ──
        int clampedScore = req.getHealthScore() != null
            ? Math.max(0, Math.min(100, req.getHealthScore())) : 70;

        String obsStr = req.getObservations() != null
            ? String.join(",", req.getObservations()) : "";

        // ISSUE-08: Fetch weather for city if provided
        String weatherCondition = fetchWeatherCondition(req.getCity());

        String smartTip = buildSmartTip(
            req.getStage() == null ? "" : req.getStage(),
            req.getObservations() == null ? Collections.emptyList() : req.getObservations(),
            clampedScore,
            req.getSoilCondition(),
            req.getStemCondition(),
            req.getLeafColour(),
            req.getSunlight(),
            req.getFertiliser(),
            userPlant.getPlantName(),
            daysNum,
            weatherCondition,
            req.getCity()
        );

        log.setStage(req.getStage());
        log.setHealthScore(clampedScore);
        log.setWatering(req.getWatering());
        log.setSunlight(req.getSunlight());
        log.setSoilCondition(req.getSoilCondition());
        log.setLeafColour(req.getLeafColour());
        log.setStemCondition(req.getStemCondition());
        log.setObservations(obsStr);
        log.setFertiliser(req.getFertiliser());
        log.setNotes(escapeHtml(req.getNotes())); // M5: sanitize
        log.setSmartTip(smartTip);
        log.setAiAnalyzed(req.getAiAnalyzed());
        log.setAiHealthScore(req.getAiHealthScore());

        GrowthLog saved = growthLogRepository.save(log);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id",            saved.getId());
        result.put("plantId",       saved.getUserPlantId());
        result.put("plantName",     userPlant.getPlantName());
        result.put("plantEmoji",    userPlant.getPlantEmoji());
        result.put("stage",         saved.getStage());
        result.put("healthScore",   clampedScore);
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
        result.put("aiAnalyzed",    saved.getAiAnalyzed());
        result.put("aiHealthScore", saved.getAiHealthScore());
        result.put("loggedAt",      saved.getLoggedAt());
        return result;
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

    // ── STAGE VALIDATION ─────────────────────────────────────────────────────
    private static final Map<String, Double> STAGE_MIN_PROP = new java.util.LinkedHashMap<>();
    private static final Map<String, Integer> HARVEST_DAYS_APPROX = new HashMap<>();
    static {
        STAGE_MIN_PROP.put("Just Planted",  0.00);
        STAGE_MIN_PROP.put("Sprouting",     0.07);
        STAGE_MIN_PROP.put("Growing",       0.22);
        STAGE_MIN_PROP.put("Leafy & Full",  0.46);
        STAGE_MIN_PROP.put("Flowering",     0.65);
        STAGE_MIN_PROP.put("Harvest Ready", 0.82);

        HARVEST_DAYS_APPROX.put("Cherry Tomatoes", 75);
        HARVEST_DAYS_APPROX.put("Tomato", 80);
        HARVEST_DAYS_APPROX.put("Tulsi (Holy Basil)", 60);
        HARVEST_DAYS_APPROX.put("Spinach (Palak)", 40);
        HARVEST_DAYS_APPROX.put("Coriander (Dhania)", 35);
        HARVEST_DAYS_APPROX.put("Mint (Pudina)", 30);
        HARVEST_DAYS_APPROX.put("Fenugreek (Methi)", 25);
        HARVEST_DAYS_APPROX.put("Radish (Mooli)", 30);
        HARVEST_DAYS_APPROX.put("Cucumber (Kheera)", 50);
        HARVEST_DAYS_APPROX.put("Okra (Bhindi)", 55);
        HARVEST_DAYS_APPROX.put("Chilli (Mirchi)", 80);
        HARVEST_DAYS_APPROX.put("Rose", 90);
        HARVEST_DAYS_APPROX.put("Marigold (Genda)", 55);
        HARVEST_DAYS_APPROX.put("Sunflower", 70);
    }

    private String checkStageForDays(String stage, String plantName, long days) {
        if (stage == null || stage.isBlank() || days <= 0) return null;
        String stageClean = stage.replaceAll("^\\S+\\s+", "").trim();
        Double minProp = STAGE_MIN_PROP.get(stageClean);
        if (minProp == null || minProp == 0.0) return null;
        int harvestDays = HARVEST_DAYS_APPROX.getOrDefault(plantName, 90);
        double currentRatio = (double) days / harvestDays;
        if (currentRatio < minProp * 0.5) {
            return "⚠️ Stage mismatch: " + plantName + " is only " + days + " days old but marked as \""
                   + stageClean + "\". Expected to reach this stage around day "
                   + (int)(minProp * harvestDays) + ".";
        }
        return null;
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
        m.put("observations",  log.getObservations() == null || log.getObservations().isBlank()
                               ? Collections.emptyList()
                               : java.util.Arrays.stream(log.getObservations().split(","))
                                   .map(String::trim).filter(s -> !s.isBlank())
                                   .collect(Collectors.toList()));
        m.put("fertiliser",    log.getFertiliser());
        m.put("notes",         log.getNotes());
        m.put("smartTip",      log.getSmartTip());
        m.put("aiAnalyzed",    log.getAiAnalyzed());
        m.put("aiHealthScore", log.getAiHealthScore());
        m.put("loggedAt",      log.getLoggedAt());
        return m;
    }

    // ── SMART TIP ENGINE (mirrors _buildSmartTip from HTML) ─────────────────
    private String buildSmartTip(String stage, List<String> obs, int healthVal,
                                  String soil, String stem, String leaf, String sun,
                                  String fert, String plant, long days,
                                  String weatherCondition, String city) {
        // ── ISSUE-08: Weather-aware watering override ──
        if (weatherCondition != null) {
            boolean isRainy = weatherCondition.equalsIgnoreCase("Rainy")
                           || weatherCondition.equalsIgnoreCase("Drizzle")
                           || weatherCondition.equalsIgnoreCase("Thunderstorm");
            if (isRainy) {
                String loc = (city != null && !city.isBlank()) ? city : "your area";
                return "It's raining in " + loc + " today — skip watering! Rain will nourish your plant naturally. "
                     + "Ensure good drainage to prevent waterlogging.";
            }
        }

        // ── ISSUE-01: Normalize condition strings (handles AI "Very Dry" / tracker "very_dry") ──
        String soilNorm = soil == null ? "" : soil.toLowerCase().replace(" ", "_");
        String stemNorm = stem == null ? "" : stem.toLowerCase().replace(" ", "_");
        String leafNorm = leaf == null ? "" : leaf.toLowerCase().replace(" ", "_");
        String sunNorm  = sun  == null ? "" : sun.toLowerCase().replace(" ", "_");

        // ── Monsoon-aware tips (June–September India) ──
        int month = java.time.LocalDate.now().getMonthValue();
        boolean isMonsoon = (month >= 6 && month <= 9);
        if (isMonsoon && "waterlogged".equals(soilNorm))
            return "Monsoon root rot alert! Immediately move to elevated pot with drainage holes. Add perlite or coarse sand to soil mix.";
        if (isMonsoon && ("wet".equals(soilNorm) || "moist".equals(soilNorm)) && obs.stream().anyMatch(o -> o.contains("Yellow") || o.contains("Drooping")))
            return "Monsoon overwatering detected. Skip watering for 3-4 days. Improve drainage — add small stones under pot.";
        if (isMonsoon && obs.stream().anyMatch(o -> o.contains("White/black spots") || o.contains("Fungal")))
            return "Monsoon fungal alert! Spray diluted neem oil (5ml + 1L water) every 5 days. Improve air circulation and avoid wetting leaves.";

        if ("waterlogged".equals(soilNorm))
            return "Root rot risk! Move to fresh well-draining soil immediately. Waterlogged roots suffocate fast.";

        if (stemNorm.contains("soft") && healthVal < 50)
            return "Soft wilting + low health = overwatering or root rot. Let soil dry completely before next watering.";

        if (obs.stream().anyMatch(o -> o.contains("Root bound")))
            return "Time to repot! Choose a pot 2 inches wider. Spring is the best time.";

        if (obs.stream().anyMatch(o -> o.contains("Pests")))
            return "Spray neem oil (5ml+1L water+2 drops dish soap) every 3 days for 2 weeks. Check leaf undersides.";

        if ("yellow".equals(leafNorm) || obs.stream().anyMatch(o -> o.contains("Yellow"))) {
            if ("wet".equals(soilNorm) || "waterlogged".equals(soilNorm))
                return "Yellow leaves + wet soil = overwatering. Let soil dry 2 inches deep before next watering.";
            return "Yellow leaves: check for overwatering, low nitrogen, or insufficient light. Try moving to a brighter spot first.";
        }

        if ("brown_tips".equals(leafNorm))
            return "Brown tips = underwatering or low humidity. Water more consistently and mist leaves in dry weather.";

        if ("light_green".equals(leafNorm))
            return "Light green leaves signal nitrogen deficiency. Apply balanced liquid fertiliser this week and ensure 4+ hours of sun.";

        if ("leggy".equals(stemNorm))
            return "Leggy growth = insufficient light. Move 1-2 feet closer to window or to a south-facing spot.";

        if ("woody".equals(stemNorm))
            return "Woody stem is normal for a maturing plant. Prune old woody branches to encourage fresh new growth.";

        if ("very_dry".equals(soilNorm) || "dry".equals(soilNorm))
            return "Soil is dry — water thoroughly until it drains from the bottom, then let top inch dry before next watering.";

        if ("none".equals(sunNorm) && stage.contains("Flower"))
            return "Flowering plants need 6+ hours of sun. No sun today may delay fruit set — move to a brighter location.";

        if ("partial".equals(sunNorm) && (stage.contains("Flower") || stage.contains("Harvest")))
            return "Fruiting and flowering stages need full sun (6+ hours). Partial sun may reduce yield — move to a sunnier spot if possible.";

        // ── Stage-specific tips (higher priority than generic "soil is fine") ──
        // "Leafy & Full" checked before individual "Leafy"/"Full" to avoid partial match
        if (stage.contains("Leafy & Full") || stage.contains("Leafy"))
            return "Pinch growing tips for bushier shape. Check if roots peek out of drainage holes — repot into a 2-inch wider pot if so.";

        Map<String, String> stageMap = new LinkedHashMap<>();
        stageMap.put("Just Planted", "Water gently and keep in indirect light for 7 days. No fertiliser for the first 2 weeks.");
        stageMap.put("Sprouting",    "Most delicate stage! Keep soil evenly moist, protect from wind and harsh direct sun.");
        stageMap.put("Growing",      "Active growth! Start diluted liquid fertiliser every 10 days. Ensure 4-6 hours of sunlight daily.");
        stageMap.put("Flowering",    "Switch to potassium feed (banana peel water works!). Reduce nitrogen. Hand-pollinate if indoors.");
        stageMap.put("Harvest",      "Harvest in the morning when flavour is best. Pick regularly to encourage more fruiting.");

        String key = stageMap.keySet().stream().filter(stage::contains).findFirst().orElse(null);
        if (key != null) return stageMap.get(key);

        // ── Fertiliser checks ──
        if (fert != null && !fert.isBlank() && !fert.contains("Not") && !fert.contains("skip"))
            return "Good job fertilising! Wait 10-14 days before the next feed to avoid over-fertilising and root burn.";

        if ((fert == null || fert.isBlank() || fert.contains("Not")) &&
            (stage.contains("Growing") || stage.contains("Flowering")))
            return "Consider adding fertiliser — " + plant + " in " + stage + " stage benefits from nutrients every 10 days.";

        // ── Everything looks fine checks ──
        if ("moist".equals(soilNorm) && healthVal >= 60)
            return "Soil moisture is perfect! Maintain this consistency — most plants thrive in evenly moist (not wet) soil.";

        if (healthVal <= 40)
            return "Health is low for " + plant + ". Check: correct sunlight? Right watering schedule? Any pests on leaf undersides?";

        if (days > 30 && healthVal >= 70)
            return "Excellent! " + plant + " is thriving after " + days + " days. Keep up the consistent care routine!";

        // ── Generic fallback: always return a useful tip ──
        if (healthVal >= 70)
            return plant + " looks good! Ensure consistent watering and 4-6 hours of appropriate sunlight daily.";
        if (healthVal >= 50)
            return plant + " needs attention. Check soil moisture, sunlight hours, and inspect leaf undersides for pests.";
        return "Health is below average for " + plant + ". Check watering schedule, move to better light, and inspect for pests or disease.";
    }

    // ISSUE-08: Fetch live weather condition for a city (returns null if city unknown or fetch fails)
    @SuppressWarnings("unchecked")
    private String fetchWeatherCondition(String city) {
        if (city == null || city.isBlank()) return null;
        String key = city.toLowerCase().trim();
        double[] coords = CITY_COORDS.get(key);
        if (coords == null) {
            // Fuzzy match
            for (Map.Entry<String, double[]> e : CITY_COORDS.entrySet()) {
                if (key.contains(e.getKey()) || e.getKey().contains(key)) {
                    coords = e.getValue();
                    break;
                }
            }
        }
        if (coords == null) return null;
        try {
            String url = "https://api.open-meteo.com/v1/forecast"
                + "?latitude=" + coords[0]
                + "&longitude=" + coords[1]
                + "&current=weather_code"
                + "&timezone=auto";
            Map<?, ?> resp = restTemplate.getForObject(url, Map.class);
            if (resp == null) return null;
            Map<?, ?> current = (Map<?, ?>) resp.get("current");
            if (current == null) return null;
            int code = ((Number) current.get("weather_code")).intValue();
            if (code >= 61 && code <= 67) return "Rainy";
            if (code >= 80 && code <= 82) return "Rainy";
            if (code >= 51 && code <= 57) return "Drizzle";
            if (code == 95 || code == 96 || code == 99) return "Thunderstorm";
            return "Clear";
        } catch (Exception e) {
            return null;
        }
    }

    // M5: HTML entity escaping to prevent XSS in user-supplied text fields
    private String escapeHtml(String input) {
        if (input == null) return null;
        return input.replace("&", "&amp;").replace("<", "&lt;")
                    .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#x27;");
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
