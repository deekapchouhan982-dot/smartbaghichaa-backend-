package com.smartbaghichaa.security;

import com.smartbaghichaa.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        String email = jwtUtil.extractEmail(token);

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            userRepository.findByEmail(email).ifPresent(user -> {
                // H1: Reject tokens issued before the last password change.
                // JWT stores iat as Unix seconds (floor), so truncate passwordChangedAt to seconds
                // before comparing — otherwise a token generated in the same second as the password
                // change would have iat (floor) < changedAt (ms), causing immediate self-invalidation.
                java.util.Date issuedAt = jwtUtil.extractIssuedAt(token);
                if (user.getPasswordChangedAt() != null && issuedAt != null) {
                    long issuedAtMs = issuedAt.getTime();
                    long changedAtMs = user.getPasswordChangedAt()
                            .truncatedTo(java.time.temporal.ChronoUnit.SECONDS)
                            .atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
                    if (issuedAtMs < changedAtMs) {
                        // Token predates last password change — silently skip (treat as unauthenticated)
                        return;
                    }
                }
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                user.getEmail(), null, Collections.emptyList());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            });
        }

        filterChain.doFilter(request, response);
    }
}
