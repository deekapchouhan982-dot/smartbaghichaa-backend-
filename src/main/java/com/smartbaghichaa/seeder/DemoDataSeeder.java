package com.smartbaghichaa.seeder;

import com.smartbaghichaa.entity.*;
import com.smartbaghichaa.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Seeds one rich demo account for presentation purposes.
 * Runs once on startup; skips if demo user already exists.
 *
 *   Email   : demo@smartbaghichaa.app
 *   Password: Demo@2025
 */
@Component
@Order(3)
public class DemoDataSeeder implements CommandLineRunner {

    @Autowired private UserRepository        userRepo;
    @Autowired private UserPlantRepository   plantRepo;
    @Autowired private GrowthLogRepository   logRepo;
    @Autowired private PlantAnalysisRepository analysisRepo;
    @Autowired private CommunityPostRepository postRepo;

    private static final String DEMO_EMAIL = "demo@smartbaghichaa.app";
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) {
        if (userRepo.findByEmail(DEMO_EMAIL).isPresent()) return; // already seeded

        // ── 1. Create demo user ──────────────────────────────────────────────
        User user = new User();
        user.setName("Deepak Chouhan");
        user.setEmail(DEMO_EMAIL);
        user.setPassword(encoder.encode("Demo@2025"));
        user.setCity("Indore");
        user.setJoinedAt(LocalDateTime.now().minusDays(30));
        user = userRepo.save(user);
        Long uid = user.getId();

        // ── 2. Add 6 plants ──────────────────────────────────────────────────
        UserPlant tulsi   = plant(uid, "Tulsi",          "🌿", "Herbs",      30);
        UserPlant tomato  = plant(uid, "Cherry Tomato",  "🍅", "Vegetables", 25);
        UserPlant rose    = plant(uid, "Rose",           "🌹", "Flowers",    20);
        UserPlant mint    = plant(uid, "Mint",           "🌱", "Herbs",      18);
        UserPlant aloe    = plant(uid, "Aloe Vera",      "🌵", "Medicinal",  15);
        UserPlant spinach = plant(uid, "Spinach",        "🥬", "Vegetables", 12);

        tulsi   = plantRepo.save(tulsi);
        tomato  = plantRepo.save(tomato);
        rose    = plantRepo.save(rose);
        mint    = plantRepo.save(mint);
        aloe    = plantRepo.save(aloe);
        spinach = plantRepo.save(spinach);

        // ── 3. Growth logs — today through 6 days ago = 7-day streak ───────
        // Streak requires a log for EACH of: today (0), yesterday (1), ..., 6 days ago (6)
        // Tulsi: logged every day for 7 days → streak = 7
        log(tulsi.getId(),  "Leafy & Full",92, "Yes", "Full Sun",    "Moist",       "Green",      "Firm",  0);
        log(tulsi.getId(),  "Leafy & Full",90, "Yes", "Full Sun",    "Moist",       "Green",      "Firm",  1);
        log(tulsi.getId(),  "Leafy & Full",88, "Yes", "Full Sun",    "Moist",       "Green",      "Firm",  2);
        log(tulsi.getId(),  "Leafy & Full",85, "Yes", "Full Sun",    "Well-drained","Green",      "Firm",  3);
        log(tulsi.getId(),  "Leafy & Full",82, "Yes", "Full Sun",    "Moist",       "Green",      "Firm",  4);
        log(tulsi.getId(),  "Growing",     78, "Yes", "Full Sun",    "Moist",       "Green",      "Firm",  5);
        log(tulsi.getId(),  "Growing",     75, "Yes", "Full Sun",    "Moist",       "Light Green","Firm",  6);

        // Cherry Tomato: 5 logs (today + 4 days back)
        log(tomato.getId(), "Fruiting",    89, "Yes", "Full Sun",    "Well-drained","Deep Green", "Firm",  0);
        log(tomato.getId(), "Flowering",   86, "Yes", "Full Sun",    "Well-drained","Green",      "Firm",  1);
        log(tomato.getId(), "Leafy & Full",82, "Yes", "Full Sun",    "Well-drained","Green",      "Firm",  2);
        log(tomato.getId(), "Growing",     78, "Yes", "Full Sun",    "Moist",       "Green",      "Firm",  3);
        log(tomato.getId(), "Growing",     75, "Yes", "Full Sun",    "Moist",       "Green",      "Firm",  4);

        // Rose: 4 logs
        log(rose.getId(),   "Flowering",   80, "Yes", "Partial Sun", "Well-drained","Green",      "Firm",  0);
        log(rose.getId(),   "Leafy & Full",77, "Yes", "Partial Sun", "Moist",       "Green",      "Firm",  1);
        log(rose.getId(),   "Growing",     73, "Yes", "Partial Sun", "Moist",       "Green",      "Firm",  2);
        log(rose.getId(),   "Growing",     70, "Yes", "Partial Sun", "Moist",       "Green",      "Firm",  3);

        // Mint: 3 logs
        log(mint.getId(),   "Growing",     76, "Yes", "Partial Sun", "Moist",     "Green",      "Firm", 0);
        log(mint.getId(),   "Growing",     72, "Yes", "Partial Sun", "Moist",     "Green",      "Firm", 1);
        log(mint.getId(),   "Sprouting",   65, "Yes", "Partial Sun", "Moist",     "Light Green","Soft", 2);

        // Aloe & Spinach: 2 logs each
        log(aloe.getId(),   "Growing",     83, "No",  "Indirect Light","Dry",     "Green",      "Firm", 0);
        log(aloe.getId(),   "Growing",     80, "No",  "Indirect Light","Dry",     "Green",      "Firm", 2);
        log(spinach.getId(),"Growing",     74, "Yes", "Partial Sun", "Moist",     "Green",      "Firm", 0);
        log(spinach.getId(),"Sprouting",   68, "Yes", "Partial Sun", "Moist",     "Light Green","Soft", 1);

        // ── 4. AI Analyses (populate the AI Health Summary section) ──────────
        analysis(uid, tulsi.getId(),   "Tulsi",        92, "Leafy & Full", "on-track",  "Healthy",     "Vibrant dark-green leaves, strong stems",   "Nutrient-rich dark soil, good drainage",    "Very strong, upright",  7);
        analysis(uid, tomato.getId(),  "Cherry Tomato",89, "Fruiting",     "ahead",     "Healthy",     "Deep green, no blemishes",                  "Rich loamy soil, well-drained",             "Strong, no disease",     2);
        analysis(uid, rose.getId(),    "Rose",         80, "Flowering",    "on-track",  "Healthy",     "Slightly pale but healthy",                 "Clay loam, slightly dry surface",           "Strong, minor scratches", 1);
        analysis(uid, mint.getId(),    "Mint",         76, "Growing",      "on-track",  "Healthy",     "Light green, aromatic",                     "Moist soil, slightly compact",              "Thin but healthy",       1);

        // ── 5. Community posts from demo user ────────────────────────────────
        communityPost("Deepak Chouhan", DEMO_EMAIL,
            "Just hit a 7-day streak on Smart Baghichaa! 🌿 My Tulsi is thriving — deep green leaves, strong stems, and fragrant every morning. The AI tracker caught early signs of overwatering last week and suggested cutting back. Worked perfectly. If you're growing herbs at home, this app is a game changer!",
            "Tulsi,Streak,HerbGarden,AITracker", 12, 1, 2);

        communityPost("Deepak Chouhan", DEMO_EMAIL,
            "Cherry Tomato update — Day 25! 🍅 Finally seeing fruits forming on the vine. Started from seed on my Indore terrace with direct sunlight and daily logging. The AI analysis confirmed I'm AHEAD of schedule! Tip: use well-drained soil + full sun + log every single day for best results.",
            "Tomatoes,Terrace,Update,Indore", 18, 0, 4);

        communityPost("Deepak Chouhan", DEMO_EMAIL,
            "Balcony garden transformation — 1 month progress 🌱→🌳\n\nStarted with 6 plants: Tulsi, Cherry Tomato, Rose, Mint, Aloe Vera & Spinach. Smart Baghichaa recommendations matched my Indore climate perfectly. Best investment of time this semester — growing your own food is incredibly satisfying. Who else is doing balcony gardening in Madhya Pradesh?",
            "BalconyGarden,Progress,Indore,MadhyaPradesh", 34, 2, 7);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private UserPlant plant(Long uid, String name, String emoji, String category, int daysAgo) {
        UserPlant p = new UserPlant();
        p.setUserId(uid);
        p.setPlantName(name);
        p.setPlantEmoji(emoji);
        p.setCategory(category);
        p.setAddedAt(LocalDateTime.now().minusDays(daysAgo));
        return p;
    }

    private void log(Long plantId, String stage, int health, String watered,
                     String sun, String soil, String leaf, String stem, int daysAgo) {
        GrowthLog g = new GrowthLog();
        g.setUserPlantId(plantId);
        g.setStage(stage);
        g.setHealthScore(health);
        g.setWatering(watered);
        g.setSunlight(sun);
        g.setSoilCondition(soil);
        g.setLeafColour(leaf);
        g.setStemCondition(stem);
        g.setAiAnalyzed(false);
        g.setLoggedAt(LocalDateTime.now().minusDays(daysAgo).withHour(8).withMinute(30));
        logRepo.save(g);
    }

    private void analysis(Long uid, Long plantId, String plantName,
                          int health, String stage, String growthStatus,
                          String verdict, String leafCond, String soilCond,
                          String stemCond, int daysAgo) {
        PlantAnalysis a = new PlantAnalysis();
        a.setUserPlantId(plantId);
        a.setUserEmail(DEMO_EMAIL);
        a.setPlantName(plantName);
        a.setHealthScore(health);
        a.setDetectedStage(stage);
        a.setGrowthStatus(growthStatus);
        a.setLeafCondition(leafCond);
        a.setSoilCondition(soilCond);
        a.setStemCondition(stemCond);
        a.setPestDetected(false);
        a.setDiseaseDetected(false);
        a.setOnTrack(true);
        a.setGrowthPercent(health);
        a.setExpertVerdict(verdict);
        a.setImmediateAction("Continue current care routine — plant is doing well.");
        a.setTodayTip("Water in the morning to reduce fungal risk. Check soil moisture before each watering.");
        a.setWeeklyPlan("Day 1-3: Monitor moisture. Day 4-5: Light fertiliser. Day 6-7: Prune dead leaves.");
        a.setWateringFrequency("Every 2 days");
        a.setNeedsFertiliser(false);
        a.setConfidenceScore(0.91);
        a.setAnalyzedAt(LocalDateTime.now().minusDays(daysAgo));
        analysisRepo.save(a);
    }

    private void communityPost(String name, String email, String content,
                               String tags, int likes, int dislikes, int daysAgo) {
        // Skip if this exact content already exists (idempotent)
        List<CommunityPost> existing = postRepo.findAll();
        boolean duplicate = existing.stream()
            .anyMatch(p -> p.getAuthorEmail().equals(email) && p.getContent().equals(content));
        if (duplicate) return;

        CommunityPost p = new CommunityPost();
        p.setAuthorName(name);
        p.setAuthorEmail(email);
        p.setContent(content);
        p.setTags(tags);
        p.setLikes(likes);
        p.setDislikes(dislikes);
        p.setCreatedAt(LocalDateTime.now().minusDays(daysAgo));
        postRepo.save(p);
    }
}
