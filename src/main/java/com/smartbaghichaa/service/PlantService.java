package com.smartbaghichaa.service;

import com.smartbaghichaa.dto.RecommendRequest;
import com.smartbaghichaa.entity.Plant;
import com.smartbaghichaa.repository.PlantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlantService {

    @Autowired
    private PlantRepository plantRepository;

    // ── GET ALL PLANTS ──────────────────────────────────────────────────────
    public List<Map<String, Object>> getAllPlants() {
        List<Plant> plants = plantRepository.findAll();
        return plants.stream().map(this::toMap).collect(Collectors.toList());
    }

    // ── SEARCH PLANTS ───────────────────────────────────────────────────────
    public List<Map<String, Object>> search(String term) {
        if (term == null || term.isBlank()) return getAllPlants();
        return plantRepository.searchByTerm(term.trim())
            .stream().map(this::toMap).collect(Collectors.toList());
    }

    // ── RECOMMEND PLANTS ────────────────────────────────────────────────────
    public List<Map<String, Object>> recommend(RecommendRequest req) {
        String city       = req.getCity() == null ? "" : req.getCity();
        String season     = req.getSeason() == null ? "" : req.getSeason();
        String space      = req.getSpace() == null ? "" : req.getSpace();
        String experience = req.getExperience() == null ? "beginner" : req.getExperience().toLowerCase();
        List<String> prefs = req.getPreferences() == null ? Collections.emptyList() : req.getPreferences();

        String seasonCode = getSeasonCode(season);
        String spaceCode  = getSpaceCode(space);
        String climate    = getClimate(city);

        List<Plant> allPlants = plantRepository.findAll();

        // ── SCORE each plant ──
        Map<Plant, Integer> scoreMap      = new HashMap<>();
        Map<Plant, Boolean> seasonMatchMap = new HashMap<>();
        Map<Plant, Boolean> climateMatchMap = new HashMap<>();

        Map<String, List<String>> adjSeasonMap = new HashMap<>();
        adjSeasonMap.put("Su", Arrays.asList("Sp", "Mo"));
        adjSeasonMap.put("Mo", Arrays.asList("Su", "Au"));
        adjSeasonMap.put("Au", Arrays.asList("Mo", "Wi"));
        adjSeasonMap.put("Wi", Arrays.asList("Au", "Sp"));
        adjSeasonMap.put("Sp", Arrays.asList("Wi", "Su"));
        List<String> adjSeasons = adjSeasonMap.getOrDefault(seasonCode, Collections.emptyList());

        for (Plant p : allPlants) {
            List<String> pSeasons  = splitField(p.getSeasons());
            List<String> pClimates = splitField(p.getClimates());
            List<String> pSpaces   = splitField(p.getSpaces());
            List<String> pPrefs    = splitField(p.getPrefs());

            int score = 0;

            // 1. SEASON (40 pts)
            boolean seasonMatch = pSeasons.contains(seasonCode);
            boolean seasonAdj   = !seasonMatch && adjSeasons.stream().anyMatch(pSeasons::contains);
            if (seasonMatch)       score += 40;
            else if (seasonAdj)    score += 12;
            else                   score -= 35;

            // 2. CLIMATE (30 pts)
            boolean climateMatch = pClimates.contains(climate);
            if (climateMatch) score += 30;
            else              score -= 20;

            // 3. SPACE (25 pts)
            if (pSpaces.contains(spaceCode)) score += 25;
            else                             score -= 30;

            // 4. PREFERENCES (12 pts each)
            if (!prefs.isEmpty()) {
                int prefHit = 0;
                for (String pref : prefs) {
                    if (pPrefs.contains(pref)) { score += 12; prefHit++; }
                }
                if (prefHit == 0) score -= 8;
            }

            // 5. EXPERIENCE
            if ("beginner".equals(experience)) {
                if      ("Very Easy".equals(p.getCareLevel())) score += 15;
                else if ("Easy".equals(p.getCareLevel()))      score += 8;
                else if ("Moderate".equals(p.getCareLevel()))  score -= 10;
                else                                            score -= 25;
            } else if ("experienced".equals(experience)) {
                if ("Moderate".equals(p.getCareLevel())) score += 5;
            }

            scoreMap.put(p, score);
            seasonMatchMap.put(p, seasonMatch);
            climateMatchMap.put(p, climateMatch);
        }

        // ── HARD FILTER 1: SPACE ──
        List<Plant> results = allPlants.stream()
            .filter(p -> splitField(p.getSpaces()).contains(spaceCode))
            .collect(Collectors.toList());

        // ── HARD FILTER 2: EXPERIENCE ──
        if ("beginner".equals(experience)) {
            results = results.stream()
                .filter(p -> !"Hard".equals(p.getCareLevel()))
                .collect(Collectors.toList());
        }

        // ── SORT by score ──
        results.sort((a, b) -> scoreMap.getOrDefault(b, 0) - scoreMap.getOrDefault(a, 0));

        // ── TIER SPLIT ──
        List<Plant> tier1 = results.stream()
            .filter(p -> Boolean.TRUE.equals(seasonMatchMap.get(p)) && Boolean.TRUE.equals(climateMatchMap.get(p)))
            .collect(Collectors.toList());
        List<Plant> tier2 = results.stream()
            .filter(p -> !(Boolean.TRUE.equals(seasonMatchMap.get(p)) && Boolean.TRUE.equals(climateMatchMap.get(p))))
            .collect(Collectors.toList());

        int t2threshold = prefs.size() > 0 ? 45 : 55;
        List<Plant> goodTier2 = tier2.stream()
            .filter(p -> scoreMap.getOrDefault(p, 0) >= t2threshold)
            .collect(Collectors.toList());

        // ── BUILD FINAL ──
        int maxResults = 18;
        List<Plant> finalList = new ArrayList<>(tier1);
        int slots = maxResults - finalList.size();
        if (slots > 0) {
            goodTier2.stream().limit(slots).forEach(finalList::add);
        }

        // Safety net: minimum 3
        if (finalList.size() < 3) {
            finalList = results.stream().limit(6).collect(Collectors.toList());
        }

        finalList = finalList.stream().limit(maxResults).collect(Collectors.toList());

        return finalList.stream().map(p -> {
            Map<String, Object> m = toMap(p);
            m.put("score", scoreMap.getOrDefault(p, 0));
            return m;
        }).collect(Collectors.toList());
    }

    // ── HELPERS ──────────────────────────────────────────────────────────────

    private Map<String, Object> toMap(Plant p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",          p.getId());
        m.put("name",        p.getName());
        m.put("emoji",       p.getEmoji());
        m.put("category",    p.getCategory());
        m.put("description", p.getDescription());
        m.put("water",       p.getWater());
        m.put("sun",         p.getSun());
        m.put("careLevel",   p.getCareLevel());
        m.put("seasons",     splitField(p.getSeasons()));
        m.put("climates",    splitField(p.getClimates()));
        m.put("spaces",      splitField(p.getSpaces()));
        m.put("prefs",       splitField(p.getPrefs()));
        m.put("tip",         p.getTip());
        return m;
    }

    private List<String> splitField(String value) {
        if (value == null || value.isBlank()) return Collections.emptyList();
        return Arrays.stream(value.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
    }

    private String getSeasonCode(String season) {
        String s = season.toLowerCase();
        if (s.contains("summer"))  return "Su";
        if (s.contains("autumn"))  return "Au";
        if (s.contains("post-"))   return "Au";
        if (s.contains("october")) return "Au";
        if (s.contains("winter"))  return "Wi";
        if (s.contains("spring"))  return "Sp";
        if (s.contains("monsoon") || s.contains("rainy") || s.contains("rain")) return "Mo";
        return "Au";
    }

    private String getSpaceCode(String space) {
        String s = space.toLowerCase();
        if (s.contains("small balcony"))             return "SB";
        if (s.contains("large balcony"))             return "LB";
        if (s.contains("terrace") || s.contains("rooftop")) return "Te";
        if (s.contains("indoor") || s.contains("window"))   return "In";
        if (s.contains("backyard") || s.contains("open"))   return "BG";
        // Also handle short codes directly
        if (s.equals("sb")) return "SB";
        if (s.equals("lb")) return "LB";
        if (s.equals("te")) return "Te";
        if (s.equals("in")) return "In";
        if (s.equals("bg")) return "BG";
        return "LB";
    }

    private String getClimate(String city) {
        Map<String, String> climateMap = buildClimateMap();
        String c = city.toLowerCase().trim();
        if (climateMap.containsKey(c)) return climateMap.get(c);
        for (Map.Entry<String, String> entry : climateMap.entrySet()) {
            if (c.contains(entry.getKey()) || entry.getKey().contains(c)) {
                return entry.getValue();
            }
        }
        return "hot_dry";
    }

    private Map<String, String> buildClimateMap() {
        Map<String, String> m = new LinkedHashMap<>();
        // hot_dry
        String[] hotDry = {"indore","bhopal","jaipur","jodhpur","nagpur","aurangabad","ahmedabad",
            "surat","ujjain","ratlam","dewas","gwalior","kota","bikaner","udaipur","ajmer",
            "nashik","solapur","amravati","jabalpur"};
        for (String c : hotDry) m.put(c, "hot_dry");
        // humid_sub
        String[] humidSub = {"delhi","new delhi","lucknow","kanpur","agra","varanasi","patna",
            "allahabad","prayagraj","meerut","noida","gurgaon","gurugram","faridabad","chandigarh",
            "jalandhar","ludhiana","amritsar","raipur"};
        for (String c : humidSub) m.put(c, "humid_sub");
        // tropical
        String[] tropical = {"mumbai","pune","goa","panaji","mangalore","kochi","thiruvananthapuram",
            "chennai","kolkata","hyderabad","bengaluru","bangalore","mysuru","mysore","coimbatore",
            "madurai","trichy","salem"};
        for (String c : tropical) m.put(c, "tropical");
        // coastal
        String[] coastal = {"visakhapatnam","bhubaneswar","puri","pondicherry","puducherry",
            "vijayawada","guntur","rajahmundry","kakinada"};
        for (String c : coastal) m.put(c, "coastal");
        // highland
        String[] highland = {"shimla","mussoorie","darjeeling","ooty","manali","nainital",
            "gangtok","shillong","srinagar","leh","dehradun"};
        for (String c : highland) m.put(c, "highland");
        return m;
    }
}
