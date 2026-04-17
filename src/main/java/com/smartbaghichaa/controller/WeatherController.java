package com.smartbaghichaa.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final RestTemplate restTemplate = new RestTemplate();

    // ── City name → [latitude, longitude] for Indian cities ─────────────────
    private static final Map<String, double[]> CITY_COORDS = new LinkedHashMap<>();
    static {
        CITY_COORDS.put("mumbai",             new double[]{19.0760,  72.8777});
        CITY_COORDS.put("delhi",              new double[]{28.6139,  77.2090});
        CITY_COORDS.put("new delhi",          new double[]{28.6139,  77.2090});
        CITY_COORDS.put("bangalore",          new double[]{12.9716,  77.5946});
        CITY_COORDS.put("bengaluru",          new double[]{12.9716,  77.5946});
        CITY_COORDS.put("hyderabad",          new double[]{17.3850,  78.4867});
        CITY_COORDS.put("chennai",            new double[]{13.0827,  80.2707});
        CITY_COORDS.put("kolkata",            new double[]{22.5726,  88.3639});
        CITY_COORDS.put("pune",               new double[]{18.5204,  73.8567});
        CITY_COORDS.put("ahmedabad",          new double[]{23.0225,  72.5714});
        CITY_COORDS.put("jaipur",             new double[]{26.9124,  75.7873});
        CITY_COORDS.put("lucknow",            new double[]{26.8467,  80.9462});
        CITY_COORDS.put("kanpur",             new double[]{26.4499,  80.3319});
        CITY_COORDS.put("nagpur",             new double[]{21.1458,  79.0882});
        CITY_COORDS.put("indore",             new double[]{22.7196,  75.8577});
        CITY_COORDS.put("bhopal",             new double[]{23.2599,  77.4126});
        CITY_COORDS.put("patna",              new double[]{25.5941,  85.1376});
        CITY_COORDS.put("vadodara",           new double[]{22.3072,  73.1812});
        CITY_COORDS.put("surat",              new double[]{21.1702,  72.8311});
        CITY_COORDS.put("coimbatore",         new double[]{11.0168,  76.9558});
        CITY_COORDS.put("visakhapatnam",      new double[]{17.6868,  83.2185});
        CITY_COORDS.put("vishakhapatnam",     new double[]{17.6868,  83.2185});
        CITY_COORDS.put("kochi",              new double[]{ 9.9312,  76.2673});
        CITY_COORDS.put("thiruvananthapuram", new double[]{ 8.5241,  76.9366});
        CITY_COORDS.put("goa",                new double[]{15.2993,  74.1240});
        CITY_COORDS.put("panaji",             new double[]{15.4909,  73.8278});
        CITY_COORDS.put("chandigarh",         new double[]{30.7333,  76.7794});
        CITY_COORDS.put("shimla",             new double[]{31.1048,  77.1734});
        CITY_COORDS.put("dehradun",           new double[]{30.3165,  78.0322});
        CITY_COORDS.put("amritsar",           new double[]{31.6340,  74.8723});
        CITY_COORDS.put("ludhiana",           new double[]{30.9010,  75.8573});
        CITY_COORDS.put("jodhpur",            new double[]{26.2389,  73.0243});
        CITY_COORDS.put("udaipur",            new double[]{24.5854,  73.7125});
        CITY_COORDS.put("raipur",             new double[]{21.2514,  81.6296});
        CITY_COORDS.put("bhubaneswar",        new double[]{20.2961,  85.8245});
        CITY_COORDS.put("jabalpur",           new double[]{23.1815,  79.9864});
        CITY_COORDS.put("ujjain",             new double[]{23.1765,  75.7885});
        CITY_COORDS.put("gwalior",            new double[]{26.2183,  78.1828});
        CITY_COORDS.put("mysuru",             new double[]{12.2958,  76.6394});
        CITY_COORDS.put("mysore",             new double[]{12.2958,  76.6394});
        CITY_COORDS.put("madurai",            new double[]{ 9.9252,  78.1198});
        CITY_COORDS.put("agra",               new double[]{27.1767,  78.0081});
        CITY_COORDS.put("varanasi",           new double[]{25.3176,  82.9739});
        CITY_COORDS.put("noida",              new double[]{28.5355,  77.3910});
        CITY_COORDS.put("gurgaon",            new double[]{28.4595,  77.0266});
        CITY_COORDS.put("gurugram",           new double[]{28.4595,  77.0266});
        CITY_COORDS.put("vijayawada",         new double[]{16.5062,  80.6480});
        CITY_COORDS.put("darjeeling",         new double[]{27.0410,  88.2663});
        CITY_COORDS.put("ooty",               new double[]{11.4102,  76.6950});
        CITY_COORDS.put("manali",             new double[]{32.2432,  77.1892});
        CITY_COORDS.put("srinagar",           new double[]{34.0837,  74.7973});
        CITY_COORDS.put("ranchi",             new double[]{23.3441,  85.3096});
        CITY_COORDS.put("puducherry",         new double[]{11.9416,  79.8083});
    }

    // ── GET /api/weather/{city} ──────────────────────────────────────────────
    @GetMapping("/{city}")
    public ResponseEntity<?> getWeather(@PathVariable String city) {
        String key = city.toLowerCase().trim().replace("-", " ");
        double[] coords = CITY_COORDS.get(key);

        // Fuzzy match if exact key not found
        if (coords == null) {
            for (Map.Entry<String, double[]> e : CITY_COORDS.entrySet()) {
                if (key.contains(e.getKey()) || e.getKey().contains(key)) {
                    coords = e.getValue();
                    break;
                }
            }
        }

        if (coords != null) {
            Map<String, Object> live = fetchFromOpenMeteo(coords[0], coords[1], titleCase(city));
            if (live != null) return ResponseEntity.ok(live);
        }

        // ISSUE-13: Unknown city — return error instead of falling back to India center coords
        if (coords == null) {
            return ResponseEntity.status(404).body(Map.of(
                "error", "City not found. Please check the city name or use a major Indian city."));
        }

        return ResponseEntity.ok(mockWeather(city));
    }

    // ── GET /api/weather/coords?lat={lat}&lon={lon} ──────────────────────────
    @GetMapping("/coords")
    public ResponseEntity<?> getWeatherByCoords(
            @RequestParam double lat,
            @RequestParam double lon) {
        Map<String, Object> live = fetchFromOpenMeteo(lat, lon, "Your Location");
        if (live != null) return ResponseEntity.ok(live);

        // Fallback mock
        Map<String, Object> fallback = new LinkedHashMap<>();
        fallback.put("city",      "Your Location");
        fallback.put("temp",      29);
        fallback.put("feelsLike", 31);
        fallback.put("humidity",  60);
        fallback.put("windSpeed", 12);
        fallback.put("condition", "Partly Cloudy");
        fallback.put("emoji",     "⛅");
        fallback.put("advice",    gardeningAdvice(29, "Partly Cloudy"));
        fallback.put("source",    "mock");
        return ResponseEntity.ok(fallback);
    }

    // ── Fetch live data from Open-Meteo (no API key required) ───────────────
    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchFromOpenMeteo(double lat, double lon, String cityName) {
        try {
            String url = "https://api.open-meteo.com/v1/forecast"
                + "?latitude="  + lat
                + "&longitude=" + lon
                + "&current=temperature_2m,apparent_temperature,relative_humidity_2m,weather_code,wind_speed_10m"
                + "&timezone=auto"
                + "&wind_speed_unit=kmh";

            Map<?, ?> resp = restTemplate.getForObject(url, Map.class);
            if (resp == null) return null;

            Map<?, ?> current = (Map<?, ?>) resp.get("current");
            if (current == null) return null;

            int    temp      = (int) Math.round(((Number) current.get("temperature_2m")).doubleValue());
            int    feelsLike = (int) Math.round(((Number) current.get("apparent_temperature")).doubleValue());
            int    humidity  = ((Number) current.get("relative_humidity_2m")).intValue();
            int    windSpeed = (int) Math.round(((Number) current.get("wind_speed_10m")).doubleValue());
            int    code      = ((Number) current.get("weather_code")).intValue();

            String condition = wmoToCondition(code);
            String emoji     = conditionEmoji(condition);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("city",      cityName);
            result.put("temp",      temp);
            result.put("feelsLike", feelsLike);
            result.put("humidity",  humidity);
            result.put("windSpeed", windSpeed);
            result.put("condition", condition);
            result.put("emoji",     emoji);
            result.put("advice",    gardeningAdvice(temp, condition));
            result.put("source",    "live");
            return result;

        } catch (Exception e) {
            System.err.println("[Weather] Open-Meteo failed for " + cityName + ": " + e.getMessage());
            return null;
        }
    }

    // ── WMO Weather Code → friendly condition name ───────────────────────────
    // Full code table: https://open-meteo.com/en/docs#weathervariables
    private String wmoToCondition(int code) {
        if (code == 0)                          return "Sunny";
        if (code <= 2)                          return "Partly Cloudy";
        if (code == 3)                          return "Cloudy";
        if (code == 45 || code == 48)           return "Foggy";
        if (code >= 51 && code <= 55)           return "Drizzle";
        if (code >= 56 && code <= 57)           return "Drizzle";
        if (code >= 61 && code <= 67)           return "Rainy";
        if (code >= 71 && code <= 77)           return "Cloudy";
        if (code >= 80 && code <= 82)           return "Rainy";
        if (code == 85 || code == 86)           return "Cloudy";
        if (code == 95)                         return "Thunderstorm";
        if (code == 96 || code == 99)           return "Thunderstorm";
        return "Partly Cloudy";
    }

    // ── Condition → Emoji ────────────────────────────────────────────────────
    private String conditionEmoji(String cond) {
        if (cond == null) return "🌤️";
        switch (cond) {
            case "Sunny":          return "☀️";
            case "Partly Cloudy":  return "⛅";
            case "Cloudy":         return "☁️";
            case "Rainy":          return "🌧️";
            case "Drizzle":        return "🌦️";
            case "Thunderstorm":   return "⛈️";
            case "Foggy":          return "🌫️";
            case "Hazy":           return "🌫️";
            case "Windy":          return "💨";
            case "Hot":            return "🌡️";
            default:               return "🌤️";
        }
    }

    // ── Gardening advice based on temp + condition ───────────────────────────
    private String gardeningAdvice(int temp, String condition) {
        if (condition == null) condition = "";
        String c = condition.toLowerCase();
        if (c.contains("thunderstorm"))
            return "⛈️ Keep plants sheltered — bring indoor plants away from windows";
        if (c.contains("rain") || c.contains("drizzle"))
            return "🌧️ Skip watering today — rain will take care of your plants!";
        if (c.contains("fog") || c.contains("foggy"))
            return "🌫️ Good moisture today — succulents and cacti don't need water";
        if (temp > 38)
            return "🔥 Extreme heat — water twice daily and move sensitive plants to shade";
        if (temp >= 33)
            return "☀️ Very hot — water morning and evening, check soil moisture";
        if (temp >= 28)
            return "🌱 Good growing day — check soil moisture before watering";
        if (temp >= 22)
            return "✨ Perfect gardening weather! Ideal for transplanting";
        if (temp >= 15)
            return "🌿 Cool and pleasant — great day for outdoor gardening";
        return "🧥 Cold day — protect sensitive plants and reduce watering";
    }

    // ── Mock fallback (kept as safety net) ──────────────────────────────────
    private static final String[] CONDITIONS = {
        "Sunny", "Rainy", "Hazy", "Cloudy", "Hot", "Partly Cloudy", "Windy"
    };
    private static final Map<String, int[]> MOCK = new LinkedHashMap<>();
    static {
        MOCK.put("indore",             new int[]{32, 45, 0, 12});
        MOCK.put("bhopal",             new int[]{29, 55, 5, 10});
        MOCK.put("mumbai",             new int[]{28, 80, 1, 18});
        MOCK.put("delhi",              new int[]{25, 60, 2, 14});
        MOCK.put("bangalore",          new int[]{22, 65, 3, 11});
        MOCK.put("bengaluru",          new int[]{22, 65, 3, 11});
        MOCK.put("chennai",            new int[]{35, 75, 4, 16});
        MOCK.put("jaipur",             new int[]{30, 40, 0, 15});
        MOCK.put("pune",               new int[]{27, 60, 5, 13});
        MOCK.put("hyderabad",          new int[]{30, 55, 0, 12});
        MOCK.put("kolkata",            new int[]{32, 78, 1, 17});
    }

    private Map<String, Object> mockWeather(String city) {
        String key  = city.toLowerCase().trim().replace("-", " ");
        int[]  data = MOCK.get(key);
        int    temp      = data != null ? data[0] : 28;
        int    humidity  = data != null ? data[1] : 60;
        String cond      = data != null ? CONDITIONS[data[2]] : "Sunny";
        int    windSpeed = data != null && data.length > 3 ? data[3] : 12;
        int    feelsLike = temp + (humidity > 70 ? 3 : humidity > 50 ? 1 : -1);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("city",      titleCase(city));
        result.put("temp",      temp);
        result.put("feelsLike", feelsLike);
        result.put("humidity",  humidity);
        result.put("windSpeed", windSpeed);
        result.put("condition", cond);
        result.put("emoji",     conditionEmoji(cond));
        result.put("advice",    gardeningAdvice(temp, cond));
        result.put("source",    "mock");
        return result;
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
