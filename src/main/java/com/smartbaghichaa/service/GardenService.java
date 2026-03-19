package com.smartbaghichaa.service;

import com.smartbaghichaa.dto.AddPlantRequest;
import com.smartbaghichaa.entity.Plant;
import com.smartbaghichaa.entity.User;
import com.smartbaghichaa.entity.UserPlant;
import com.smartbaghichaa.repository.PlantRepository;
import com.smartbaghichaa.repository.UserPlantRepository;
import com.smartbaghichaa.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GardenService {

    @Autowired
    private UserPlantRepository userPlantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlantRepository plantRepository;

    // ── GET GARDEN ─────────────────────────────────────────────────────────
    public List<Map<String, Object>> getGarden(String email) {
        User user = findUser(email);
        List<UserPlant> plants = userPlantRepository.findByUserId(user.getId());

        return plants.stream().map(up -> {
            // Enrich with plants table data if emoji/category missing
            String emoji    = up.getPlantEmoji();
            String category = up.getCategory();
            if ((emoji == null || emoji.isBlank()) || (category == null || category.isBlank())) {
                Optional<Plant> p = plantRepository.findByNameIgnoreCase(up.getPlantName());
                if (p.isPresent()) {
                    if (emoji    == null || emoji.isBlank())    emoji    = p.get().getEmoji();
                    if (category == null || category.isBlank()) category = p.get().getCategory();
                    // Update stored values to avoid repeated lookups
                    up.setPlantEmoji(emoji);
                    up.setCategory(category);
                    userPlantRepository.save(up);
                }
            }

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",        up.getId());
            m.put("plantName", up.getPlantName());
            m.put("plantEmoji", emoji);
            m.put("category",  category);
            m.put("addedAt",   up.getAddedAt());
            long days = ChronoUnit.DAYS.between(
                up.getAddedAt() == null ? LocalDateTime.now() : up.getAddedAt(),
                LocalDateTime.now());
            m.put("daysSinceAdded", days);
            return m;
        }).collect(Collectors.toList());
    }

    // ── ADD PLANT ──────────────────────────────────────────────────────────
    public Map<String, Object> addPlant(String email, AddPlantRequest req) {
        User user = findUser(email);

        if (userPlantRepository.existsByUserIdAndPlantName(user.getId(), req.getPlantName())) {
            throw new IllegalStateException("Already in your garden");
        }

        UserPlant up = new UserPlant();
        up.setUserId(user.getId());
        up.setPlantName(req.getPlantName());
        up.setPlantEmoji(req.getPlantEmoji());
        up.setCategory(req.getCategory());

        UserPlant saved = userPlantRepository.save(up);

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",        saved.getId());
        m.put("plantName", saved.getPlantName());
        m.put("plantEmoji",saved.getPlantEmoji());
        m.put("category",  saved.getCategory());
        m.put("addedAt",   saved.getAddedAt());
        m.put("message",   "Plant added to your garden!");
        return m;
    }

    // ── DELETE PLANT ───────────────────────────────────────────────────────
    public Map<String, Object> deletePlant(String email, Long plantId) {
        User user = findUser(email);

        UserPlant up = userPlantRepository.findByIdAndUserId(plantId, user.getId())
            .orElseThrow(() -> new IllegalArgumentException("Plant not found in your garden"));

        userPlantRepository.delete(up);

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("message",   "Plant removed from your garden");
        m.put("plantName", up.getPlantName());
        return m;
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
