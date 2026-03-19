package com.smartbaghichaa.config;

import com.smartbaghichaa.security.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(unauthorizedEntryPoint())
            )
            .authorizeHttpRequests(auth -> auth
                // Public auth endpoints
                .requestMatchers(HttpMethod.POST, "/api/auth/signup").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/forgot-password").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/auth/reset-password").permitAll()
                // Static content
                .requestMatchers("/", "/index.html", "/static/**",
                                 "/*.js", "/*.css", "/*.ico",
                                 "/*.json", "/*.png").permitAll()
                // Weather API - public
                .requestMatchers(HttpMethod.GET, "/api/weather/**").permitAll()
                // Protected endpoints
                .requestMatchers(HttpMethod.PUT,  "/api/auth/profile").authenticated()
                .requestMatchers(HttpMethod.GET,  "/api/auth/me").authenticated()
                .requestMatchers("/api/garden/**").authenticated()
                .requestMatchers("/api/reminders/**").authenticated()
                .requestMatchers("/api/tracker/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/community/posts").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/community/posts/*/like").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/community/posts/*/dislike").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/community/posts/*/comments").authenticated()
                // Everything else permitted
                .anyRequest().permitAll()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Unauthorized — please log in\",\"status\":401}");
        };
    }
}
