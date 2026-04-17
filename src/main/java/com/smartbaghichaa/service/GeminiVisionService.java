package com.smartbaghichaa.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartbaghichaa.dto.PlantAnalysisResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class GeminiVisionService {

    private static final Logger log = LoggerFactory.getLogger(GeminiVisionService.class);

    @Value("${gemini.api.key:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper  = new ObjectMapper();

    // ── Harvest days (days from planting to first harvest/maturity) ────────────
    private static final Map<String, Integer> HARVEST_DAYS = new HashMap<>();
    // ── Stage day ranges per plant ────────────────────────────────────────────
    private static final Map<String, Map<String, int[]>> STAGE_DAYS = new HashMap<>();

    static {
        HARVEST_DAYS.put("Cherry Tomatoes",          75);
        HARVEST_DAYS.put("Tulsi (Holy Basil)",       60);
        HARVEST_DAYS.put("Spinach (Palak)",          40);
        HARVEST_DAYS.put("Rose",                     90);
        HARVEST_DAYS.put("Marigold (Genda)",         55);
        HARVEST_DAYS.put("Chilli (Mirchi)",          80);
        HARVEST_DAYS.put("Coriander (Dhania)",       35);
        HARVEST_DAYS.put("Aloe Vera",               120);
        HARVEST_DAYS.put("Money Plant (Pothos)",    180);
        HARVEST_DAYS.put("Sunflower",                70);
        HARVEST_DAYS.put("Mint (Pudina)",            30);
        HARVEST_DAYS.put("Tomato",                   80);
        HARVEST_DAYS.put("Brinjal (Baingan)",        90);
        HARVEST_DAYS.put("Okra (Bhindi)",            55);
        HARVEST_DAYS.put("Bitter Gourd (Karela)",    70);
        HARVEST_DAYS.put("Bottle Gourd (Lauki)",     65);
        HARVEST_DAYS.put("Cucumber (Kheera)",        50);
        HARVEST_DAYS.put("Pumpkin (Kaddu)",          90);
        HARVEST_DAYS.put("Fenugreek (Methi)",        25);
        HARVEST_DAYS.put("Radish (Mooli)",           30);
        HARVEST_DAYS.put("Carrot (Gajar)",           70);
        HARVEST_DAYS.put("Onion (Pyaaz)",           120);
        HARVEST_DAYS.put("Garlic (Lahsun)",         150);
        HARVEST_DAYS.put("Ginger (Adrak)",          240);
        HARVEST_DAYS.put("Turmeric (Haldi)",        270);
        HARVEST_DAYS.put("Lemon (Nimbu)",           365);
        HARVEST_DAYS.put("Curry Leaf (Kadi Patta)", 180);
        HARVEST_DAYS.put("Banana",                  365);
        HARVEST_DAYS.put("Papaya",                  270);
        HARVEST_DAYS.put("Hibiscus (Gurhal)",        90);
        HARVEST_DAYS.put("Jasmine (Mogra)",         120);
        HARVEST_DAYS.put("Lavender",                 90);
        HARVEST_DAYS.put("Snake Plant",             365);
        HARVEST_DAYS.put("Peace Lily",              365);
        HARVEST_DAYS.put("Spider Plant",            180);
        HARVEST_DAYS.put("Jade Plant",              365);
        HARVEST_DAYS.put("Cactus",                  365);
        HARVEST_DAYS.put("Succulent Mix",           365);
        HARVEST_DAYS.put("Bamboo",                  365);
        HARVEST_DAYS.put("Lucky Bamboo",            365);
        HARVEST_DAYS.put("Rubber Plant",            365);
        HARVEST_DAYS.put("Ficus",                   365);
        HARVEST_DAYS.put("Adenium (Desert Rose)",   180);
        HARVEST_DAYS.put("Bougainvillea",           120);
        HARVEST_DAYS.put("Chrysanthemum (Shevanti)", 90);
        HARVEST_DAYS.put("Portulaca (Gul Mehndi)",   45);
        HARVEST_DAYS.put("Petunia",                  60);
        HARVEST_DAYS.put("Zinnia",                   60);
        HARVEST_DAYS.put("Pansy",                    60);
        HARVEST_DAYS.put("Dahlia",                   90);
        HARVEST_DAYS.put("Mogra (Jasmine)",         120);

        // Cherry Tomatoes stage ranges
        Map<String, int[]> ct = new HashMap<>();
        ct.put("Just Planted",  new int[]{0,  7});
        ct.put("Sprouting",     new int[]{8,  21});
        ct.put("Growing",       new int[]{22, 45});
        ct.put("Leafy & Full",  new int[]{46, 60});
        ct.put("Flowering",     new int[]{61, 70});
        ct.put("Harvest Ready", new int[]{71, 90});
        STAGE_DAYS.put("Cherry Tomatoes", ct);

        // Tulsi stage ranges
        Map<String, int[]> tl = new HashMap<>();
        tl.put("Just Planted",  new int[]{0,  7});
        tl.put("Sprouting",     new int[]{8,  14});
        tl.put("Growing",       new int[]{15, 30});
        tl.put("Leafy & Full",  new int[]{31, 45});
        tl.put("Flowering",     new int[]{46, 55});
        tl.put("Harvest Ready", new int[]{56, 90});
        STAGE_DAYS.put("Tulsi (Holy Basil)", tl);

    }

    // ── Stage proportions as fraction of total harvest period (plant-agnostic) ──
    // Used to compute per-plant stage ranges proportionally from their harvest days.
    private static final java.util.LinkedHashMap<String, double[]> STAGE_PROPORTIONS =
        new java.util.LinkedHashMap<>();
    static {
        STAGE_PROPORTIONS.put("Just Planted",  new double[]{0.00, 0.07});
        STAGE_PROPORTIONS.put("Sprouting",     new double[]{0.07, 0.22});
        STAGE_PROPORTIONS.put("Growing",       new double[]{0.22, 0.46});
        STAGE_PROPORTIONS.put("Leafy & Full",  new double[]{0.46, 0.65});
        STAGE_PROPORTIONS.put("Flowering",     new double[]{0.65, 0.82});
        STAGE_PROPORTIONS.put("Harvest Ready", new double[]{0.82, 1.00});
    }

    // ── Health score lower-bounds per critical symptom ────────────────────────
    // If AI returns a higher score than the cap, we clamp it down.
    private static final Map<String, Integer> LEAF_HEALTH_CAP = new HashMap<>();
    static {
        LEAF_HEALTH_CAP.put("Healthy Green", 100);
        LEAF_HEALTH_CAP.put("Light Green",    85);
        LEAF_HEALTH_CAP.put("Yellow",         65);
        LEAF_HEALTH_CAP.put("Brown Tips",     60);
        LEAF_HEALTH_CAP.put("Wilting",        50);
        LEAF_HEALTH_CAP.put("Damaged",        45);
    }
    private static final Map<String, Integer> SOIL_HEALTH_PENALTY = new HashMap<>();
    static {
        SOIL_HEALTH_PENALTY.put("Very Dry",    15);
        SOIL_HEALTH_PENALTY.put("Waterlogged", 10);
        SOIL_HEALTH_PENALTY.put("Dry",          5);
        SOIL_HEALTH_PENALTY.put("Moist",        0);
        SOIL_HEALTH_PENALTY.put("Wet",          0);
    }

    // ── Public entry point ────────────────────────────────────────────────────
    public PlantAnalysisResult analyzePhoto(String base64Image, String plantName,
                                             int daysSincePlanted, String userSelectedStage,
                                             Integer userHealthScore, String userCity) {
        boolean photoProvided = base64Image != null && !base64Image.isBlank();

        if (apiKey == null || apiKey.isBlank()) {
            return analyzeWithoutAI(plantName, daysSincePlanted, userSelectedStage, userHealthScore, photoProvided);
        }
        String mimeType = detectMimeType(base64Image);
        String prompt   = buildExpertPrompt(plantName, daysSincePlanted, userSelectedStage, userCity);
        try {
            // gemini-2.5-flash: latest stable, fast, full vision support
            String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                       + "gemini-2.5-flash:generateContent?key=" + apiKey;
            Map<String, Object> requestBody = buildGeminiRequest(prompt, base64Image, mimeType);
            // Try RestTemplate first; fall back to curl if JVM networking is blocked by firewall
            String aiText = null;
            try {
                ResponseEntity<Map> response = restTemplate.postForEntity(url, requestBody, Map.class);
                aiText = extractTextFromGemini(response.getBody());
            } catch (Exception restEx) {
                log.warn("[Gemini] RestTemplate failed ({}), retrying via curl...", restEx.getClass().getSimpleName());
                aiText = callGeminiViaCurl(url, requestBody);
            }
            if (aiText != null && aiText.contains("```")) {
                aiText = aiText.replaceAll("(?s)```json\\s*", "").replaceAll("(?s)```\\s*", "").trim();
            }
            return parseAIResponse(aiText, plantName, daysSincePlanted, userSelectedStage);
        } catch (Exception e) {
            log.warn("[Gemini] API call failed for {} — {}: {}", plantName, e.getClass().getSimpleName(), e.getMessage());
            return analyzeWithoutAI(plantName, daysSincePlanted, userSelectedStage, userHealthScore, photoProvided);
        }
    }

    // ── Season + climate context lookup (month → Indian season + typical conditions) ─
    private static final Map<String, String> MONTH_CONTEXT = new HashMap<>();
    static {
        MONTH_CONTEXT.put("January",   "Winter (Rabi). Temp 8-20°C. Low humidity. Frost risk in North India nights. Minimal watering needed. Protect tender plants.");
        MONTH_CONTEXT.put("February",  "Late Winter. Temp 12-25°C. Dry air. Good growing weather. Watch for aphids as temps rise.");
        MONTH_CONTEXT.put("March",     "Spring (Zaid begins). Temp 18-32°C. Rising heat. Increase watering frequency. Risk of powdery mildew in humid areas.");
        MONTH_CONTEXT.put("April",     "Summer (Zaid). Temp 30-42°C. Very low humidity in North/Central India. High heat stress. Water deeply in morning. Shade cloth recommended. Soil dries out extremely fast.");
        MONTH_CONTEXT.put("May",       "Peak Summer. Temp 35-45°C. Scorching heat. Water twice daily for most plants. Mulch heavily. Risk of sunscald, heat wilt, and spider mites.");
        MONTH_CONTEXT.put("June",      "Pre-Monsoon/Early Monsoon. Temp 30-40°C. Humidity rising fast. Watch for fungal disease. Reduce fertiliser. Improve drainage.");
        MONTH_CONTEXT.put("July",      "Monsoon (Kharif). High humidity 80-95%. Temp 28-35°C. Overwatering risk. Fungal diseases peak. Ensure drainage. Spray copper fungicide preventively.");
        MONTH_CONTEXT.put("August",    "Peak Monsoon. Heavy rainfall. Waterlogging risk. Root rot common. Reduce watering entirely for potted plants. Watch for blight and damping-off.");
        MONTH_CONTEXT.put("September", "Late Monsoon. Humidity still high. Temp 28-34°C. Good growth window. Fertilise now. Watch for aphids as rains ease.");
        MONTH_CONTEXT.put("October",   "Post-Monsoon (Rabi begins). Temp 22-32°C. Ideal growing season. Best time for most vegetables. Regular watering, good sun.");
        MONTH_CONTEXT.put("November",  "Early Winter. Temp 15-26°C. Excellent growing weather. Low pest pressure. Good time for transplanting. Reduce watering slightly.");
        MONTH_CONTEXT.put("December",  "Winter. Temp 8-18°C. Cold nights. Reduce watering. Protect tropicals from frost. Slow growth phase.");
    }

    // ── Ornamental/houseplants that don't have a harvest ─────────────────────
    private static final java.util.Set<String> NO_HARVEST_PLANTS = new java.util.HashSet<>(java.util.Arrays.asList(
        "Snake Plant", "Peace Lily", "Spider Plant", "Money Plant (Pothos)", "Jade Plant",
        "Cactus", "Succulent Mix", "Bamboo", "Lucky Bamboo", "Rubber Plant", "Ficus",
        "Adenium (Desert Rose)", "Bougainvillea", "Hibiscus (Gurhal)", "Rose",
        "Marigold (Genda)", "Chrysanthemum (Shevanti)", "Portulaca (Gul Mehndi)",
        "Petunia", "Zinnia", "Pansy", "Dahlia", "Sunflower", "Lavender",
        "Jasmine (Mogra)", "Mogra (Jasmine)"
    ));

    // ── Prompt ────────────────────────────────────────────────────────────────
    private String buildExpertPrompt(String plantName, int daysSincePlanted, String userStage, String userCity) {
        String month = java.time.Month.from(java.time.LocalDate.now())
                .getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH);
        String location      = (userCity != null && !userCity.isBlank()) ? userCity + ", India" : "India";
        String expectedStage = getExpectedStageForDays(plantName, daysSincePlanted);
        int    harvestDays   = HARVEST_DAYS.getOrDefault(plantName, 90);
        String seasonCtx     = MONTH_CONTEXT.getOrDefault(month, "India. Adjust advice for local conditions.");
        boolean isOrnamental = NO_HARVEST_PLANTS.contains(plantName);
        String harvestNote   = isOrnamental
            ? plantName + " is an ornamental/houseplant — set daysToHarvest to -1."
            : plantName + " matures in " + harvestDays + " days total — estimate days remaining.";

        return
          // ── PERSONA ─────────────────────────────────────────────────────────
          "You are Dr. Krishi, a plant diagnostician trained at IARI (Indian Agricultural Research Institute). "
        + "You give honest, evidence-based diagnoses. You do NOT reassure users — you report exactly what you see. "
        + "Overestimating plant health has caused users to neglect sick plants until it was too late. Be accurate, not kind.\n\n"

          // ── STEP 1: IDENTITY CHECK ───────────────────────────────────────────
        + "STEP 1 — IDENTITY CHECK.\n"
        + "If the image does NOT show a plant, leaf, flower, seedling, stem, soil with a plant, or any vegetation, respond ONLY with:\n"
        + "{\"isPlant\": false, \"notPlantReason\": \"describe exactly what the image shows\"}\n\n"

          // ── STEP 2: CONTEXT ──────────────────────────────────────────────────
        + "STEP 2 — PLANT CONTEXT.\n"
        + "Plant: " + plantName + " | Age: " + daysSincePlanted + " days | Location: " + location + "\n"
        + "Month: " + month + " — " + seasonCtx + "\n"
        + "Expected growth stage at day " + daysSincePlanted + ": \"" + expectedStage + "\"\n"
        + "Owner reports stage: \"" + userStage + "\"\n"
        + harvestNote + "\n\n"

          // ── STEP 3: SYSTEMATIC VISUAL EXAMINATION ───────────────────────────
        + "STEP 3 — EXAMINE THE IMAGE CAREFULLY in this order:\n"
        + "① LEAVES — colour uniformity (healthy=rich dark green). Yellowing pattern matters:\n"
        + "   • Tips only → underwatering or salt buildup\n"
        + "   • Older/lower leaves only → nitrogen deficiency or natural senescence\n"
        + "   • Overall pale/light green → insufficient light or general nutrient deficiency\n"
        + "   • Spots (brown=fungal, black=bacterial, rust-orange=rust disease)\n"
        + "   • Curling inward=heat/drought stress; curling outward=overwatering/root rot\n"
        + "② STEM — firmness (soft at base=root rot), elongation (leggy=low light), internode distance\n"
        + "③ SOIL — cracked/pulling away from pot=Very Dry; dark+wet=Wet/Waterlogged; white crust=salt; green algae=overwatering\n"
        + "④ PESTS — webbing between leaves=spider mites; white fluff=mealybugs; sticky residue=aphids/scale; tiny holes in leaves=thrips\n"
        + "⑤ DISEASE — powdery white coating=powdery mildew; water-soaked dark patches=blight; orange pustules=rust; base rot=damping-off\n"
        + "⑥ SIZE/FULLNESS — is the plant appropriately sized for " + daysSincePlanted + " days? Too small=stunted; too tall/thin=etiolated\n\n"

          // ── STEP 4: SCORING CALIBRATION (internal, before JSON) ─────────────
        + "STEP 4 — HEALTH SCORE (commit to this BEFORE writing JSON).\n"
        + "Start from the worst thing you see:\n"
        + "  • Nothing wrong, lush and full → 88-95\n"
        + "  • Healthy but slightly underfed/pale, or mildly dry soil → 75-85\n"
        + "  • Visible yellowing on multiple leaves OR clearly dry/stressed → 58-72\n"
        + "  • Active pest/disease signs OR significant wilting/damage → 38-55\n"
        + "  • Severe rot, near-collapse, extensive damage → 15-35\n"
        + "Then apply adjustments:\n"
        + "  +5 dense healthy new growth visible\n"
        + "  -8 multiple problems co-occurring\n"
        + "  -5 poor soil condition (Very Dry or Waterlogged)\n"
        + "THIS score is your healthScore. Do not change it after writing it.\n\n"

          // ── JSON SCHEMA ──────────────────────────────────────────────────────
        + "OUTPUT — return EXACTLY this JSON object, nothing else:\n"
        + "{\n"
        + "  \"isPlant\": true,\n"
        + "  \"healthScore\": <integer 0-100 — the score you committed to in Step 4>,\n"
        + "  \"confidenceScore\": <0-100 — 90+ if plant fills frame and is well-lit; ≤50 if blurry/dark/distant>,\n"
        + "  \"expertVerdict\": <1 sentence: lead with the main finding, e.g. 'Moderate nitrogen deficiency with early spider mite signs on lower leaves' or 'Healthy vigorous plant, no issues detected'>,\n"
        + "  \"rootCause\": <for ANY score: what is the #1 factor limiting this plant's health or growth right now? Even for healthy plants: e.g. 'Optimal — could benefit from stronger direct sunlight' or 'Healthy but approaching time for fertiliser'>,\n"
        + "  \"detectedStage\": <one of: \"Just Planted\" / \"Sprouting\" / \"Growing\" / \"Leafy & Full\" / \"Flowering\" / \"Harvest Ready\">,\n"
        + "  \"stageConfirmed\": <true if owner's reported stage \"" + userStage + "\" matches what you see, false otherwise>,\n"
        + "  \"correctedStage\": <the correct stage if stageConfirmed is false, else null>,\n"
        + "  \"growthStatus\": <\"ahead\" / \"on-track\" / \"behind\" / \"concern\">,\n"
        + "  \"leafCondition\": <\"Healthy Green\" / \"Light Green\" / \"Yellow\" / \"Brown Tips\" / \"Wilting\" / \"Damaged\">,\n"
        + "  \"leafAnalysis\": <2 sentences: what exactly do the leaves look like, and what does that pattern indicate for " + plantName + " in " + month + ">,\n"
        + "  \"soilCondition\": <\"Very Dry\" / \"Dry\" / \"Moist\" / \"Wet\" / \"Waterlogged\">,\n"
        + "  \"soilFix\": <specific action: e.g. 'Water thoroughly now until it drains from the base, then wait 2-3 days before checking again' — or 'Soil moisture is fine, no action needed'>,\n"
        + "  \"stemCondition\": <\"Firm & Upright\" / \"Soft Wilting\" / \"Leggy\" / \"Woody\">,\n"
        + "  \"pestDetected\": <true/false>,\n"
        + "  \"pestDetails\": <if true: name pest + where exactly you see evidence + what damage. If false: null>,\n"
        + "  \"diseaseDetected\": <true/false>,\n"
        + "  \"diseaseDetails\": <if true: name disease + visible symptoms. If false: null>,\n"
        + "  \"immediateAction\": <the single most urgent thing to do in the next 24 hours — specific, not generic>,\n"
        + "  \"treatmentSteps\": <3-5 steps using | separator. If issues: numbered actionable steps using Indian remedies (neem oil, jeevamrit, vermicompost). If healthy: '1. Maintain current watering schedule|2. Feed with balanced fertiliser in 1 week|3. Rotate pot every 3 days for even sun exposure'>,\n"
        + "  \"todayTip\": <1 specific tip for " + month + " in " + location + " — must mention the season condition: " + seasonCtx.split("\\.")[0] + ">,\n"
        + "  \"careSchedule\": <7-day schedule based on what YOU observed — format: 'Mon:action|Tue:action|Wed:action|Thu:action|Fri:action|Sat:action|Sun:action'. Actions must reflect the diagnosis (e.g. if pest found: include neem spray day; if dry: include watering days). Max 8 words per action>,\n"
        + "  \"weeklyPlan\": <2 sentences: what to prioritise this week based on what you saw, and what to watch for>,\n"
        + "  \"wateringFrequency\": <choose based on soilCondition — Very Dry/Dry: \"today\" or \"tomorrow\"; Moist: \"in 2 days\" or \"in 3 days\"; Wet/Waterlogged: \"in 3 days\" or \"weekly\". Options: \"today\" / \"tomorrow\" / \"in 2 days\" / \"in 3 days\" / \"twice a week\" / \"weekly\">,\n"
        + "  \"needsFertiliser\": <true if plant shows signs of nutrient deficiency OR has not been fed in likely 3+ weeks based on age>,\n"
        + "  \"fertiliserIn\": <\"today\" / \"3 days\" / \"1 week\" / \"2 weeks\" / \"not needed\">,\n"
        + "  \"daysToHarvest\": <" + (isOrnamental ? "-1 — this is an ornamental plant" : "integer: estimated days remaining to harvest/maturity") + ">\n"
        + "}\n\n"

          // ── SELF-CONSISTENCY CHECK ───────────────────────────────────────────
        + "BEFORE SUBMITTING — verify these constraints or correct the JSON:\n"
        + "✓ If leafCondition is \"Yellow\" or \"Wilting\" → healthScore must be ≤65\n"
        + "✓ If pestDetected OR diseaseDetected is true → healthScore must be ≤60\n"
        + "✓ If soilCondition is \"Very Dry\" → wateringFrequency must be \"today\" or \"tomorrow\"\n"
        + "✓ If soilCondition is \"Waterlogged\" → wateringFrequency must be \"weekly\" or \"in 3 days\"\n"
        + "✓ If confidenceScore ≤50 → mention image quality issue in expertVerdict\n"
        + "✓ careSchedule must have exactly 7 entries (Mon through Sun) separated by |\n"
        + "✓ treatmentSteps must use | between steps, no newline characters\n"
        + "Return ONLY the JSON object. No markdown. No text before or after.";
    }

    // ── curl fallback: used when JVM networking is blocked by Windows Firewall ──
    private String callGeminiViaCurl(String url, Map<String, Object> requestBody) {
        try {
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            java.io.File tmpFile = java.io.File.createTempFile("gemini_req_", ".json");
            tmpFile.deleteOnExit();
            java.nio.file.Files.writeString(tmpFile.toPath(), jsonBody, java.nio.charset.StandardCharsets.UTF_8);
            log.info("[Gemini-curl] Temp file: {}", tmpFile.getAbsolutePath());

            ProcessBuilder pb = new ProcessBuilder(
                "curl", "-s", "-X", "POST", url,
                "-H", "Content-Type: application/json",
                "--data-binary", "@" + tmpFile.getAbsolutePath(),
                "--max-time", "30"
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            String response = new String(process.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            int exitCode = process.waitFor();
            tmpFile.delete();
            log.info("[Gemini-curl] Exit={}, ResponseLen={}, FullResponse='{}'",
                exitCode, response.length(), response.replace("\n", " ").replace("\r", ""));

            if (exitCode != 0 || response.isBlank()) {
                log.warn("[Gemini] curl fallback failed with exit code {}", exitCode);
                return null;
            }
            // Retry once on 503 (model overloaded) after a short wait
            if (response.contains("\"code\": 503") || response.contains("\"code\":503")) {
                log.warn("[Gemini] 503 overloaded — retrying in 4 seconds...");
                Thread.sleep(4000);
                process = pb.start();
                response = new String(process.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                process.waitFor();
            }
            Map<?, ?> responseMap = objectMapper.readValue(response, Map.class);
            String extracted = extractTextFromGemini(responseMap);
            log.info("[Gemini-curl] Extracted text length: {}", extracted != null ? extracted.length() : "null");
            return extracted;
        } catch (Exception e) {
            log.warn("[Gemini] curl fallback error: {} — {}", e.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    // ── Build Gemini API request ───────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private Map<String, Object> buildGeminiRequest(String prompt, String base64Image, String mimeType) {
        Map<String, Object> body    = new HashMap<>();
        Map<String, Object> content = new HashMap<>();
        List<Map<String, Object>> parts = new ArrayList<>();

        // Image FIRST — Gemini Vision attends better when image precedes the analysis instruction
        if (base64Image != null && !base64Image.isBlank()) {
            Map<String, Object> imagePart  = new HashMap<>();
            Map<String, Object> inlineData = new HashMap<>();
            inlineData.put("mime_type", mimeType);
            inlineData.put("data", base64Image);
            imagePart.put("inline_data", inlineData);
            parts.add(imagePart);
        }

        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);
        parts.add(textPart);

        content.put("parts", parts);
        body.put("contents", List.of(content));

        Map<String, Object> config = new HashMap<>();
        config.put("temperature", 0.1);
        config.put("maxOutputTokens", 4096);   // 2500 was too tight — careSchedule+treatmentSteps alone can hit 400 tokens
        config.put("topP", 0.95);              // tighter sampling for structured JSON fields
        config.put("response_mime_type", "application/json");  // forces valid JSON — Gemini API uses snake_case
        body.put("generationConfig", config);

        return body;
    }

    // ── Extract text from Gemini response ─────────────────────────────────────
    @SuppressWarnings("unchecked")
    private String extractTextFromGemini(Map<?, ?> body) {
        if (body == null) return null;
        try {
            List<?> candidates = (List<?>) body.get("candidates");
            if (candidates == null || candidates.isEmpty()) return null;
            Map<?, ?> candidate = (Map<?, ?>) candidates.get(0);
            Map<?, ?> content   = (Map<?, ?>) candidate.get("content");
            if (content == null) return null;
            List<?>   parts     = (List<?>) content.get("parts");
            if (parts == null || parts.isEmpty()) return null;
            Map<?, ?> part      = (Map<?, ?>) parts.get(0);
            return (String) part.get("text");
        } catch (Exception e) {
            return null;
        }
    }

    // ── Parse AI JSON response ─────────────────────────────────────────────────
    private PlantAnalysisResult parseAIResponse(String jsonResponse, String plantName,
                                                 int daysSincePlanted, String userStage) {
        if (jsonResponse == null || jsonResponse.isBlank()) {
            return analyzeWithoutAI(plantName, daysSincePlanted, userStage, null, true);
        }
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);

            // Non-plant image
            if (!root.path("isPlant").asBoolean(true)) {
                PlantAnalysisResult notPlant = new PlantAnalysisResult();
                notPlant.setIsPlant(false);
                notPlant.setNotPlantReason(root.path("notPlantReason").asText("No plant detected in the image."));
                notPlant.setAiPowered(true);
                notPlant.setHealthScore(0);
                notPlant.setConfidenceScore(0.0);
                return notPlant;
            }

            PlantAnalysisResult result = new PlantAnalysisResult();
            result.setIsPlant(true);
            result.setAiPowered(true);

            // ── Normalize conditions (handles case/format variations from AI) ──
            String leafCond    = normalizeLeafCondition(root.path("leafCondition").asText("Healthy Green"));
            String soilCond    = normalizeSoilCondition(root.path("soilCondition").asText("Moist"));
            String stemCond    = normalizeStemCondition(root.path("stemCondition").asText("Firm & Upright"));
            boolean hasPest    = root.path("pestDetected").asBoolean(false);
            boolean hasDisease = root.path("diseaseDetected").asBoolean(false);
            int aiHealthScore  = root.path("healthScore").asInt(70);

            // ── Sanity guardrails: only apply soil penalty + leaf cap ──────────
            // Pest/disease penalty removed: the AI prompt already instructs Gemini
            // to subtract ≥20 pts for pests/disease — adding another 15 here was double-counting.
            int soilPenalty    = SOIL_HEALTH_PENALTY.getOrDefault(soilCond, 0);
            int healthCap      = LEAF_HEALTH_CAP.getOrDefault(leafCond, 100);
            int sanitisedScore = Math.max(0, aiHealthScore - soilPenalty);
            sanitisedScore     = Math.min(sanitisedScore, healthCap);

            result.setHealthScore(sanitisedScore);
            result.setLeafCondition(leafCond);
            result.setSoilCondition(soilCond);
            result.setStemCondition(stemCond);
            result.setPestDetected(hasPest);
            result.setPestDetails(root.path("pestDetails").isNull() ? null : root.path("pestDetails").asText(null));
            result.setDiseaseDetected(hasDisease);
            result.setDiseaseDetails(root.path("diseaseDetails").isNull() ? null : root.path("diseaseDetails").asText(null));

            // ── Stage: validate AI's answer against actual plant age ───────────
            String rawStage      = root.path("detectedStage").asText("Growing");
            String detectedStage = validateStageForAge(plantName, rawStage, daysSincePlanted);
            result.setDetectedStage(detectedStage);
            result.setGrowthStatus(root.path("growthStatus").asText("on-track"));

            // ── Stage correction ───────────────────────────────────────────────
            boolean stageConfirmed = root.path("stageConfirmed").asBoolean(true);
            String correctedStage  = root.path("correctedStage").isNull() ? null
                                   : root.path("correctedStage").asText(null);
            // If our age-based validation overrode AI's stage, update correction info
            if (!detectedStage.equals(rawStage)) {
                stageConfirmed = false;
                correctedStage = detectedStage;
            }
            result.setStageConfirmed(stageConfirmed);
            result.setCorrectedStage(correctedStage);

            result.setExpertVerdict(root.path("expertVerdict").asText(null));
            result.setRootCause(root.path("rootCause").asText(null));
            result.setTreatmentSteps(root.path("treatmentSteps").isNull() ? null : root.path("treatmentSteps").asText(null));
            result.setCareSchedule(root.path("careSchedule").isNull() ? null : root.path("careSchedule").asText(null));
            result.setLeafAnalysis(root.path("leafAnalysis").isNull() ? null : root.path("leafAnalysis").asText(null));
            result.setSoilFix(root.path("soilFix").isNull() ? null : root.path("soilFix").asText(null));

            result.setImmediateAction(root.path("immediateAction").asText("Keep monitoring your plant daily."));
            result.setTodayTip(root.path("todayTip").asText("Check soil moisture before watering."));
            result.setWeeklyPlan(root.path("weeklyPlan").asText("Water regularly and ensure adequate sunlight."));
            result.setWateringFrequency(root.path("wateringFrequency").asText("tomorrow"));
            result.setNeedsFertiliser(root.path("needsFertiliser").asBoolean(false));
            result.setFertiliserIn(root.path("fertiliserIn").asText("not needed"));

            // ── Reconcile daysToHarvest weighted by AI confidence ─────────────
            double conf = root.path("confidenceScore").asDouble(80);
            result.setConfidenceScore(conf);
            int knownHarvest  = HARVEST_DAYS.getOrDefault(plantName, 90);
            int dataBasedDTH  = Math.max(0, knownHarvest - daysSincePlanted);
            int aiDTH         = root.path("daysToHarvest").asInt(dataBasedDTH);
            // AI weight scales with confidence (0-40%); data weight is the rest
            int aiWeight      = (int) Math.min(40, conf * 0.40);
            int reconciledDTH = (dataBasedDTH * (100 - aiWeight) + aiDTH * aiWeight) / 100;
            result.setDaysToHarvest(Math.max(0, reconciledDTH));

            result.setGrowthPercent(calculateGrowthPercent(plantName, daysSincePlanted, detectedStage));
            return result;

        } catch (Exception e) {
            return analyzeWithoutAI(plantName, daysSincePlanted, userStage, null, true);
        }
    }

    // ── Growth % calculation ──────────────────────────────────────────────────
    // 70% weight on actual days (truth), 30% on stage position (cross-check)
    private int calculateGrowthPercent(String plantName, int days, String stage) {
        int harvestDays = HARVEST_DAYS.getOrDefault(plantName, 90);
        int daysPct = Math.min(100, (days * 100) / Math.max(1, harvestDays));
        if (stage != null && !stage.isBlank()) {
            int[] stageRange = getProportionalStageRange(plantName, stage);
            if (stageRange != null) {
                int stageMid = (stageRange[0] + stageRange[1]) / 2;
                int stagePct = Math.min(100, (stageMid * 100) / Math.max(1, harvestDays));
                return Math.min(100, (daysPct * 70 + stagePct * 30) / 100);
            }
        }
        return daysPct;
    }

    // ── Returns stage day range for a plant (proportional if no plant-specific data) ──
    private int[] getProportionalStageRange(String plantName, String stage) {
        if (stage == null || stage.isBlank()) return null;
        // Try plant-specific ranges first
        Map<String, int[]> specific = STAGE_DAYS.get(plantName);
        if (specific != null) {
            for (Map.Entry<String, int[]> e : specific.entrySet()) {
                if (stage.contains(e.getKey()) || e.getKey().contains(stage)) return e.getValue();
            }
        }
        // Calculate proportionally from harvest days
        int harvestDays = HARVEST_DAYS.getOrDefault(plantName, 90);
        for (Map.Entry<String, double[]> e : STAGE_PROPORTIONS.entrySet()) {
            if (stage.contains(e.getKey()) || e.getKey().contains(stage)) {
                double[] pct = e.getValue();
                return new int[]{(int)(harvestDays * pct[0]), (int)(harvestDays * pct[1])};
            }
        }
        return null;
    }

    // ── Derives the expected stage for a plant at a given age ─────────────────
    private String getExpectedStageForDays(String plantName, int days) {
        int harvestDays = HARVEST_DAYS.getOrDefault(plantName, 90);
        double pct = (double) days / Math.max(1, harvestDays);
        for (Map.Entry<String, double[]> e : STAGE_PROPORTIONS.entrySet()) {
            double[] range = e.getValue();
            if (pct >= range[0] && pct < range[1]) return e.getKey();
        }
        return pct >= 1.0 ? "Harvest Ready" : "Just Planted";
    }

    // ── Validates AI stage against actual age; corrects obvious mismatches ────
    private String validateStageForAge(String plantName, String aiStage, int days) {
        if (aiStage == null || aiStage.isBlank()) return getExpectedStageForDays(plantName, days);
        int[] range = getProportionalStageRange(plantName, aiStage);
        if (range == null) return aiStage;
        // Allow ±7 days from stage boundaries to catch genuine early/late bloomers
        int tolerance = 7;
        if (days <= range[1] + tolerance && days >= Math.max(0, range[0] - 7)) return aiStage;
        return getExpectedStageForDays(plantName, days);
    }

    // ── Condition normalizers (handle AI output format/casing variations) ──────
    private String normalizeLeafCondition(String raw) {
        if (raw == null || raw.isBlank()) return "Healthy Green";
        String s = raw.toLowerCase().trim().replace("_", " ");
        if (s.contains("wilt") || s.contains("droop"))      return "Wilting";
        if (s.contains("damage") || s.contains("spot") || s.contains("burn")) return "Damaged";
        if (s.contains("brown") || s.contains("tip"))        return "Brown Tips";
        if (s.contains("yellow") || s.contains("chloro"))   return "Yellow";
        if (s.contains("light green") || s.contains("pale") || s.contains("light_green")) return "Light Green";
        if (s.contains("healthy") || s.contains("dark green") || s.contains("green")) return "Healthy Green";
        return "Healthy Green";
    }

    private String normalizeSoilCondition(String raw) {
        if (raw == null || raw.isBlank()) return "Moist";
        String s = raw.toLowerCase().trim().replace("_", " ");
        if (s.contains("waterlog") || s.contains("standing") || s.contains("flood")) return "Waterlogged";
        if (s.contains("very dry") || s.contains("bone dry") || s.contains("crack"))  return "Very Dry";
        if (s.contains("wet") || s.contains("soggy") || s.contains("saturated"))      return "Wet";
        if (s.contains("dry"))                                                          return "Dry";
        return "Moist";
    }

    private String normalizeStemCondition(String raw) {
        if (raw == null || raw.isBlank()) return "Firm & Upright";
        String s = raw.toLowerCase().trim().replace("_", " ");
        if (s.contains("soft") || s.contains("wilt") || s.contains("droop") || s.contains("limp")) return "Soft Wilting";
        if (s.contains("leggy") || s.contains("stretch") || s.contains("lanky") || s.contains("etiolat")) return "Leggy";
        if (s.contains("woody") || s.contains("mature") || s.contains("hard") || s.contains("bark"))      return "Woody";
        return "Firm & Upright";
    }

    // ── Detect MIME type from base64 header ───────────────────────────────────
    private String detectMimeType(String base64) {
        if (base64 == null || base64.length() < 8) return "image/jpeg";
        // Check first few chars of raw base64 data for magic bytes
        String prefix = base64.substring(0, Math.min(20, base64.length()));
        // PNG magic: iVBOR (base64 of \x89PNG)
        if (prefix.startsWith("iVBOR")) return "image/png";
        // WEBP: UklGR
        if (prefix.startsWith("UklGR")) return "image/webp";
        // GIF: R0lGO
        if (prefix.startsWith("R0lGO")) return "image/gif";
        // Default JPEG
        return "image/jpeg";
    }

    // ── Fallback when API key is absent or call fails ─────────────────────────
    // ISSUE-12: photoProvided flag allows honest verdict when photo was submitted but AI failed.
    private PlantAnalysisResult analyzeWithoutAI(String plantName, int days, String stage,
                                                   Integer userHealthScore, boolean photoProvided) {
        int knownHarvest = HARVEST_DAYS.getOrDefault(plantName, 90);
        // Use the user's own health selector value if provided, otherwise estimate by stage
        int health;
        if (userHealthScore != null && userHealthScore > 0) {
            health = userHealthScore;
        } else if (stage == null || stage.isBlank() || stage.equals("Just Planted") || stage.equals("Sprouting")) {
            health = 80;  // early stage plants are likely healthy
        } else {
            health = 72;  // generic estimate for growing plants
        }
        // Derive leaf/soil/stem from health score so fallback isn't always optimistic
        String fallbackLeaf, fallbackSoil, fallbackStem;
        if (health >= 80)      { fallbackLeaf = "Healthy Green"; fallbackSoil = "Moist";    fallbackStem = "Firm & Upright"; }
        else if (health >= 65) { fallbackLeaf = "Light Green";   fallbackSoil = "Dry";      fallbackStem = "Firm & Upright"; }
        else if (health >= 50) { fallbackLeaf = "Yellow";        fallbackSoil = "Dry";      fallbackStem = "Soft Wilting";   }
        else                   { fallbackLeaf = "Brown Tips";    fallbackSoil = "Very Dry";  fallbackStem = "Soft Wilting";  }

        PlantAnalysisResult result = new PlantAnalysisResult();
        result.setHealthScore(health);
        result.setDetectedStage(stage != null && !stage.isBlank() ? stage : "Growing");
        result.setLeafCondition(fallbackLeaf);
        result.setSoilCondition(fallbackSoil);
        result.setStemCondition(fallbackStem);
        result.setPestDetected(false);
        result.setDiseaseDetected(false);
        result.setGrowthStatus("on-track");
        result.setGrowthPercent(calculateGrowthPercent(plantName, days, stage));
        result.setImmediateAction("Check soil moisture — push finger 1 inch in. Water only if dry.");
        result.setTodayTip("Inspect leaf undersides for early pests. Early detection prevents 90% of problems.");
        result.setWeeklyPlan("Water when top inch of soil is dry. Ensure 4-6 hours of appropriate sunlight daily. Check for pests twice a week.");
        result.setWateringFrequency("tomorrow");
        result.setNeedsFertiliser(false);
        result.setFertiliserIn("not needed");
        result.setDaysToHarvest(Math.max(0, knownHarvest - days));
        result.setConfidenceScore(50.0);
        result.setStageConfirmed(true);
        result.setCorrectedStage(null);
        result.setAiPowered(false);

        // ISSUE-12: Honest verdict — clearly distinguish "no photo" from "AI unavailable"
        if (photoProvided) {
            result.setExpertVerdict("⚠️ AI temporarily unavailable — results are rule-based estimates only. Your photo was not analyzed. Try again later.");
            result.setRootCause("AI service unavailable — rule-based estimate only. Photo was not analyzed.");
        } else {
            result.setExpertVerdict("Upload a clear photo for AI diagnosis — estimate based on plant age only.");
            result.setRootCause("No photo provided — rule-based estimate only.");
        }

        result.setTreatmentSteps("1. Upload a well-lit close-up photo|2. Ensure leaves and soil are visible|3. Take photo in daylight for best results");
        result.setCareSchedule("Mon:Water if soil dry|Tue:Check for pests|Wed:Rotate pot for even sun|Thu:Check soil moisture|Fri:Remove dead leaves|Sat:Feed if needed|Sun:Rest & observe");
        result.setLeafAnalysis("Leaf condition not assessed — photo required for accurate diagnosis.");
        result.setSoilFix("Check soil moisture by pressing finger 1 inch deep. Water thoroughly only if dry.");
        return result;
    }
}
