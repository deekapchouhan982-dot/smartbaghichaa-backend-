package com.smartbaghichaa.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/weather")
@CrossOrigin(origins = "*")
public class WeatherController {

    @Value("${openweather.api.key:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    // ── OWM city name aliases (Indian variants → OWM-accepted names) ────────
    private static final Map<String, String> OWM_ALIAS = new LinkedHashMap<>();
    static {
        OWM_ALIAS.put("bengaluru",       "Bangalore");
        OWM_ALIAS.put("new delhi",       "Delhi");
        OWM_ALIAS.put("gurugram",        "Gurgaon");
        OWM_ALIAS.put("panaji",          "Goa");
        OWM_ALIAS.put("puducherry",      "Pondicherry");
        OWM_ALIAS.put("thiruvananthapuram", "Trivandrum");
        OWM_ALIAS.put("mysuru",          "Mysore");
        OWM_ALIAS.put("vijayawada",      "Vijayawada");
        OWM_ALIAS.put("vishakhapatnam",  "Visakhapatnam");
    }

    // ── Mock fallback: city (lowercase) → [temp °C, humidity %, condIdx] ───
    private static final Map<String, int[]> MOCK = new LinkedHashMap<>();
    private static final String[] CONDITIONS = {
        "Sunny", "Humid", "Hazy", "Cloudy", "Hot", "Partly Cloudy", "Windy"
    };
    static {
        MOCK.put("indore",          new int[]{32, 45, 0});
        MOCK.put("bhopal",          new int[]{29, 55, 5});
        MOCK.put("mumbai",          new int[]{28, 80, 1});
        MOCK.put("delhi",           new int[]{25, 60, 2});
        MOCK.put("new delhi",       new int[]{25, 60, 2});
        MOCK.put("bangalore",       new int[]{22, 65, 3});
        MOCK.put("bengaluru",       new int[]{22, 65, 3});
        MOCK.put("chennai",         new int[]{35, 75, 4});
        MOCK.put("jaipur",          new int[]{30, 40, 0});
        MOCK.put("pune",            new int[]{27, 60, 5});
        MOCK.put("hyderabad",       new int[]{30, 55, 0});
        MOCK.put("kolkata",         new int[]{32, 78, 1});
        MOCK.put("ahmedabad",       new int[]{33, 45, 0});
        MOCK.put("surat",           new int[]{31, 70, 1});
        MOCK.put("nagpur",          new int[]{34, 40, 4});
        MOCK.put("lucknow",         new int[]{28, 60, 2});
        MOCK.put("kanpur",          new int[]{29, 58, 2});
        MOCK.put("patna",           new int[]{30, 65, 2});
        MOCK.put("vadodara",        new int[]{32, 50, 0});
        MOCK.put("coimbatore",      new int[]{28, 65, 3});
        MOCK.put("visakhapatnam",   new int[]{30, 70, 1});
        MOCK.put("kochi",           new int[]{29, 82, 1});
        MOCK.put("thiruvananthapuram", new int[]{29, 80, 1});
        MOCK.put("goa",             new int[]{30, 78, 1});
        MOCK.put("panaji",          new int[]{30, 78, 1});
        MOCK.put("chandigarh",      new int[]{24, 55, 5});
        MOCK.put("shimla",          new int[]{12, 60, 3});
        MOCK.put("dehradun",        new int[]{20, 55, 5});
        MOCK.put("amritsar",        new int[]{26, 50, 2});
        MOCK.put("ludhiana",        new int[]{26, 52, 2});
        MOCK.put("jodhpur",         new int[]{35, 30, 0});
        MOCK.put("udaipur",         new int[]{31, 40, 0});
        MOCK.put("raipur",          new int[]{32, 60, 5});
        MOCK.put("bhubaneswar",     new int[]{33, 70, 1});
        MOCK.put("jabalpur",        new int[]{31, 50, 0});
        MOCK.put("ujjain",          new int[]{31, 45, 0});
        MOCK.put("gwalior",         new int[]{30, 48, 2});
        MOCK.put("mysuru",          new int[]{23, 62, 3});
        MOCK.put("mysore",          new int[]{23, 62, 3});
        MOCK.put("madurai",         new int[]{34, 68, 4});
        MOCK.put("agra",            new int[]{28, 55, 2});
        MOCK.put("varanasi",        new int[]{29, 60, 2});
        MOCK.put("noida",           new int[]{25, 58, 2});
        MOCK.put("gurgaon",         new int[]{25, 55, 2});
        MOCK.put("gurugram",        new int[]{25, 55, 2});
        MOCK.put("vijayawada",      new int[]{32, 68, 1});
        MOCK.put("darjeeling",      new int[]{14, 70, 3});
        MOCK.put("ooty",            new int[]{16, 65, 3});
        MOCK.put("manali",          new int[]{8,  50, 3});
        MOCK.put("srinagar",        new int[]{14, 55, 5});
        MOCK.put("ranchi",          new int[]{26, 60, 5});
    }

    // ── GET /api/weather/{city} ─────────────────────────────────────────────
    @GetMapping("/{city}")
    public ResponseEntity<?> getWeather(@PathVariable String city) {
        // Try real API if key is configured
        if (apiKey != null && !apiKey.isBlank() && !apiKey.equals("your_key_here")) {
            Map<String, Object> live = fetchFromOWM(city);
            if (live != null) return ResponseEntity.ok(live);
        }
        // Fall back to mock data
        return ResponseEntity.ok(mockWeather(city));
    }

    // ── OpenWeatherMap real API call ────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchFromOWM(String city) {
        try {
            String key = city.toLowerCase().trim();
            String owmCity = OWM_ALIAS.getOrDefault(key, titleCase(city));
            String url = "https://api.openweathermap.org/data/2.5/weather"
                + "?q=" + java.net.URLEncoder.encode(owmCity + ",IN", "UTF-8")
                + "&appid=" + apiKey
                + "&units=metric";

            Map<?, ?> resp = restTemplate.getForObject(url, Map.class);
            if (resp == null) return null;

            Map<?, ?> main    = (Map<?, ?>) resp.get("main");
            java.util.List<?> weather = (java.util.List<?>) resp.get("weather");
            if (main == null || weather == null || weather.isEmpty()) return null;

            Map<?, ?> w0 = (Map<?, ?>) weather.get(0);

            int    temp     = (int) Math.round(((Number) main.get("temp")).doubleValue());
            int    humidity = ((Number) main.get("humidity")).intValue();
            String owmDesc  = (String) w0.get("main");   // "Clear", "Clouds", "Rain" …
            String cond     = owmToCondition(owmDesc);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("city",      titleCase(city));
            result.put("temp",      temp);
            result.put("condition", cond);
            result.put("humidity",  humidity);
            result.put("unit",      "°C");
            result.put("emoji",     conditionEmoji(cond));
            result.put("source",    "live");
            return result;

        } catch (Exception e) {
            System.err.println("[Weather] OWM API failed for " + city + ": " + e.getMessage());
            return null;
        }
    }

    // ── Mock fallback ───────────────────────────────────────────────────────
    private Map<String, Object> mockWeather(String city) {
        String key  = city.toLowerCase().trim().replace("-", " ");
        int[]  data = MOCK.get(key);

        if (data == null) {
            for (Map.Entry<String, int[]> e : MOCK.entrySet()) {
                if (key.contains(e.getKey()) || e.getKey().contains(key)) {
                    data = e.getValue();
                    break;
                }
            }
        }

        int    temp     = data != null ? data[0] : 28;
        int    humidity = data != null ? data[1] : 60;
        String cond     = data != null ? CONDITIONS[data[2]] : "Sunny";

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("city",      titleCase(city));
        result.put("temp",      temp);
        result.put("condition", cond);
        result.put("humidity",  humidity);
        result.put("unit",      "°C");
        result.put("emoji",     conditionEmoji(cond));
        result.put("source",    "mock");
        return result;
    }

    // ── OWM condition → friendly condition name ─────────────────────────────
    private String owmToCondition(String owm) {
        if (owm == null) return "Sunny";
        switch (owm) {
            case "Clear":        return "Sunny";
            case "Clouds":       return "Cloudy";
            case "Drizzle":
            case "Rain":         return "Humid";
            case "Thunderstorm": return "Cloudy";
            case "Snow":         return "Cloudy";
            case "Mist":
            case "Fog":
            case "Haze":
            case "Smoke":
            case "Dust":
            case "Sand":         return "Hazy";
            case "Squall":
            case "Tornado":      return "Windy";
            default:             return "Partly Cloudy";
        }
    }

    private String conditionEmoji(String cond) {
        switch (cond) {
            case "Sunny":         return "☀️";
            case "Humid":         return "🌧️";
            case "Hazy":          return "🌫️";
            case "Cloudy":        return "☁️";
            case "Hot":           return "🌡️";
            case "Partly Cloudy": return "⛅";
            case "Windy":         return "💨";
            default:              return "🌤️";
        }
    }

    private String titleCase(String s) {
        if (s == null || s.isEmpty()) return s;
        String[] words = s.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(Character.toUpperCase(w.charAt(0)));
                sb.append(w.substring(1).toLowerCase());
            }
        }
        return sb.toString();
    }
}
