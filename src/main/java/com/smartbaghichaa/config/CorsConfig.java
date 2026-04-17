package com.smartbaghichaa.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins:http://localhost:8080}")
    private String allowedOriginsProperty;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        List<String> origins = new ArrayList<>(Arrays.asList(allowedOriginsProperty.split(",")));
        // Always include the backend itself (HTML served from Spring Boot static)
        for (String required : List.of("http://localhost:8080", "http://127.0.0.1:8080")) {
            if (!origins.contains(required)) origins.add(required);
        }

        // Use allowedOriginPatterns so that `null` (file://) origins are also accepted during dev.
        // Each pattern is trimmed in case of extra whitespace in config.
        List<String> patterns = new ArrayList<>();
        for (String o : origins) patterns.add(o.trim());
        // Allow file:// opened HTML (origin comes as "null" string from browser)
        patterns.add("null");

        config.setAllowedOriginPatterns(patterns);
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "X-Requested-With"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
