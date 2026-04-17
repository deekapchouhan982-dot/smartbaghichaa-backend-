package com.smartbaghichaa.seeder;

import com.smartbaghichaa.entity.CommunityPost;
import com.smartbaghichaa.repository.CommunityPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
@Order(2)
public class CommunitySeeder implements CommandLineRunner {

    @Autowired
    private CommunityPostRepository postRepository;

    @Override
    public void run(String... args) {
        if (postRepository.count() > 0) return;

        List<CommunityPost> posts = Arrays.asList(
            post("Saksham Pal", "saksham@smartbaghichaa.demo",
                "My Tulsi plant doubled in size this month after following Smart Baghichaa guidance! Moved it to the east-facing balcony — the results are amazing. Highly recommend for anyone starting out.",
                "Tulsi,Balcony,Success Story", 24, 1, null,
                LocalDateTime.now().minusDays(7)),

            post("Tanuja Wala", "tanuja@smartbaghichaa.demo",
                "Tried growing Cherry Tomatoes on my terrace for the first time. The care guide was super helpful — especially the fertiliser timing. First fruits are almost ready!",
                "Tomatoes,Terrace,Beginner", 18, 0, null,
                LocalDateTime.now().minusDays(6)),

            post("Rani Yadav", "rani@smartbaghichaa.demo",
                "Pro tip: Plant Marigold alongside your vegetables — they act as natural pest repellents! My spinach crop has been pest-free since I added them. Companion planting works!",
                "Marigold,Companion Planting,Organic", 31, 2,
                "https://images.unsplash.com/photo-1599685315640-4a8da98b4e1c?w=600&q=80",
                LocalDateTime.now().minusDays(5)),

            post("Deepak C", "deepak@smartbaghichaa.demo",
                "ACHIEVEMENT UNLOCKED \uD83C\uDFC6 First full tomato harvest from my 10x10 ft terrace garden — 4.2 kg of cherry tomatoes! Started as a beginner 3 months ago with Smart Baghichaa recommendations. Never thought I could grow this much in the city. Photos don't lie!",
                "Tomatoes,Harvest,Achievement,Terrace", 47, 0,
                "https://images.unsplash.com/photo-1592921870789-04563d55041c?w=600&q=80",
                LocalDateTime.now().minusDays(2)),

            post("Tanuja Wala", "tanuja@smartbaghichaa.demo",
                "My Tulsi plant journey — 90 days in a row! \uD83C\uDF3F Started from a small seedling in a single pot on my balcony. Daily care log + Smart Baghichaa AI tips made a huge difference. The plant is now 2 feet tall and giving leaves for chai every morning. Growing your own herbs is the best decision ever!",
                "Tulsi,Balcony,HerbGarden,90DayChallenge", 39, 1,
                "https://images.unsplash.com/photo-1515150144380-bca9f1650ed9?w=600&q=80",
                LocalDateTime.now().minusDays(1))
        );

        postRepository.saveAll(posts);
    }

    private CommunityPost post(String authorName, String authorEmail,
                                String content, String tags,
                                int likes, int dislikes, String photoData,
                                LocalDateTime createdAt) {
        CommunityPost p = new CommunityPost();
        p.setAuthorName(authorName);
        p.setAuthorEmail(authorEmail);
        p.setContent(content);
        p.setTags(tags);
        p.setLikes(likes);
        p.setDislikes(dislikes);
        p.setPhotoData(photoData);
        p.setCreatedAt(createdAt);
        return p;
    }
}
