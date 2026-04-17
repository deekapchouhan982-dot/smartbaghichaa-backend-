package com.smartbaghichaa.seeder;

import com.smartbaghichaa.entity.Plant;
import com.smartbaghichaa.repository.PlantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Order(1)
public class PlantDataSeeder implements CommandLineRunner {

    @Autowired
    private PlantRepository plantRepository;

    @Override
    public void run(String... args) {
        if (plantRepository.count() > 0) return; // already seeded

        List<Plant> plants = Arrays.asList(
            new Plant("Tulsi (Holy Basil)", "🌿", "Herb & Medicinal",
                "Sacred and hardy, Tulsi thrives in Indian heat. Minimal care, grows in any pot, naturally wards off insects.",
                "Low", "Full Sun", "Easy", "Su,Mo,Au,Sp",
                "hot_dry,humid_sub,tropical,coastal,highland",
                "SB,LB,Te,BG", "pHerb,pMed",
                "Keep in 6+ hours of direct sun. Pinch flower buds to keep leaves bushy and productive."),

            new Plant("Pudina (Mint)", "🍃", "Herb",
                "Fast-spreading aromatic herb perfect for chai and cooking. Thrives in cool seasons and partial shade.",
                "Moderate", "Partial Sun", "Easy", "Au,Wi,Sp",
                "hot_dry,humid_sub,tropical,coastal,highland",
                "SB,LB,Te,In", "pHerb,pMed",
                "Grow in a separate pot — mint spreads aggressively. Keep soil consistently moist."),

            new Plant("Coriander (Dhania)", "🌿", "Herb & Vegetable",
                "Essential kitchen herb that grows quickly in cool weather. Perfect in small pots for continuous yield.",
                "Moderate", "Partial Sun", "Easy", "Au,Wi,Sp",
                "hot_dry,humid_sub,tropical,coastal",
                "SB,LB,Te", "pHerb,pVeg",
                "Sow seeds directly in pot. Harvest outer leaves only. Re-sow every 3 weeks for endless supply."),

            new Plant("Fenugreek (Methi)", "🌿", "Herb & Vegetable",
                "Highly nutritious leafy herb ready in just 3 weeks. Ideal cool-weather crop for any balcony.",
                "Moderate", "Full Sun", "Easy", "Au,Wi",
                "hot_dry,humid_sub,tropical,coastal",
                "SB,LB,Te", "pHerb,pVeg",
                "Sow densely for continuous harvest. Cut and it will regrow 2-3 times from the same pot."),

            new Plant("Curry Leaf (Kadi Patta)", "🍃", "Herb",
                "Essential in South Indian cooking. Very low maintenance once established, grows well in large pots.",
                "Low", "Full Sun", "Easy", "Su,Mo,Au,Sp",
                "hot_dry,tropical,coastal",
                "LB,Te,BG", "pHerb",
                "Start in a 12-inch pot. Fertilise monthly. Leaves are most fragrant when harvested in the morning."),

            new Plant("Lemongrass", "🌾", "Herb & Medicinal",
                "Tropical aromatic grass used in tea, cooking and as a natural mosquito repellent. Very low maintenance.",
                "Moderate", "Full Sun", "Easy", "Su,Mo,Au",
                "tropical,coastal,hot_dry",
                "LB,Te,BG", "pHerb,pMed",
                "Grows tall (3-4 ft) — needs a large pot. Divide clumps yearly to keep productive."),

            new Plant("Brahmi", "🍀", "Medicinal Herb",
                "Ancient Ayurvedic brain-boosting herb. Loves moisture and grows easily in shallow water trays.",
                "High", "Partial Shade", "Easy", "Mo,Au,Wi",
                "tropical,coastal",
                "SB,LB", "pMed,pHerb",
                "Grows beautifully in shallow trays with standing water. Perfect low-light kitchen companion."),

            new Plant("Stevia", "🍃", "Medicinal Herb",
                "Natural zero-calorie sweetener. Pluck a leaf directly into your tea. Easy container plant.",
                "Moderate", "Full Sun", "Easy", "Au,Wi,Sp",
                "tropical,coastal,hot_dry",
                "SB,LB,Te", "pMed,pHerb",
                "Pinch stem tips for bushy growth. Avoid waterlogging. Harvest leaves before flowering begins."),

            new Plant("Cherry Tomatoes", "🍅", "Vegetable & Fruit",
                "Most productive balcony vegetable. One plant gives fruit for months. Ideal for containers with support.",
                "Moderate", "Full Sun", "Moderate", "Au,Wi,Sp",
                "hot_dry,humid_sub,tropical,coastal",
                "LB,Te,BG", "pVeg,pFruit",
                "Use minimum 12-litre pot with a stick support. Pinch suckers weekly for more fruit yield."),

            new Plant("Spinach (Palak)", "🥬", "Vegetable",
                "Fast-growing leafy green ready in 30 days. Highly nutritious and perfect for cool season containers.",
                "Moderate", "Partial Sun", "Easy", "Au,Wi",
                "hot_dry,humid_sub,tropical,coastal,highland",
                "SB,LB,Te", "pVeg",
                "Sow thickly and thin seedlings. Harvest outer leaves to extend yield. Bolts and turns bitter in heat."),

            new Plant("Chilli (Mirchi)", "🌶️", "Vegetable",
                "Thrives in Indian summers. One plant can produce hundreds of chillies. Great for any pot size.",
                "Moderate", "Full Sun", "Easy", "Su,Mo,Au,Sp",
                "hot_dry,humid_sub,tropical,coastal",
                "SB,LB,Te,BG", "pVeg",
                "Very heat tolerant. Full sun is essential. One plant per 8-litre pot gives excellent yield."),

            new Plant("Brinjal (Baingan)", "🍆", "Vegetable",
                "Heat-loving, highly productive vegetable for Indian summers. Keeps bearing fruit over several months.",
                "Moderate", "Full Sun", "Moderate", "Su,Mo,Au",
                "hot_dry,humid_sub,tropical,coastal",
                "LB,Te,BG", "pVeg",
                "Needs 15-litre pot. Stake for support. Fertilise with compost every 3 weeks for heavy yield."),

            new Plant("Lady Finger (Bhindi)", "🌿", "Vegetable",
                "Heat-loving summer crop that produces abundantly. Harvest every 2 days or pods become too tough.",
                "Moderate", "Full Sun", "Easy", "Su,Mo",
                "hot_dry,humid_sub,tropical,coastal",
                "LB,Te,BG", "pVeg",
                "Direct sow seeds. Pick pods young (4-5 inches). Tolerates Indian summer heat better than most crops."),

            new Plant("Radish (Mooli)", "🥕", "Vegetable",
                "Fastest vegetable crop — ready in just 25-30 days. Perfect instant gratification for new gardeners!",
                "Moderate", "Partial Sun", "Easy", "Au,Wi",
                "hot_dry,humid_sub,tropical,coastal",
                "SB,LB,Te", "pVeg",
                "Sow directly in 8-inch deep pots. Thin to 3 inches apart. Water evenly to prevent splitting."),

            new Plant("Peas (Matar)", "🫛", "Vegetable",
                "Cool-season climbing vegetable that thrives in Indian winters. Very productive with a simple trellis.",
                "Moderate", "Full Sun", "Moderate", "Wi",
                "hot_dry,humid_sub,highland,coastal",
                "LB,Te,BG", "pVeg",
                "Sow Oct-Nov for best crop. Needs trellis or string support. Improves soil nitrogen naturally."),

            new Plant("Cucumber", "🥒", "Vegetable",
                "Water-loving summer climber that produces abundantly on a vertical trellis. Saves balcony floor space.",
                "High", "Full Sun", "Moderate", "Su,Mo",
                "hot_dry,humid_sub,tropical,coastal",
                "Te,BG", "pVeg",
                "Train vertically on trellis. Water at soil level, never on leaves. Consistent watering prevents bitterness."),

            new Plant("Capsicum (Bell Pepper)", "🫑", "Vegetable",
                "Colourful, nutritious and very productive in Indian winters. Grows well in medium containers.",
                "Moderate", "Full Sun", "Moderate", "Au,Wi,Sp",
                "hot_dry,humid_sub,tropical,coastal",
                "LB,Te,BG", "pVeg",
                "Use 10-litre pots. Pinch first flowers for bigger overall yield. Add calcium to prevent blossom end rot."),

            new Plant("Green Onion (Hara Pyaz)", "🧅", "Vegetable & Herb",
                "Regrows from roots in a glass of water or soil. Ready in weeks. No soil or tools needed.",
                "Low", "Partial Sun", "Very Easy", "Au,Wi",
                "hot_dry,humid_sub,tropical,coastal,highland",
                "SB,LB,Te,In", "pVeg,pHerb",
                "Plant onion roots in shallow water. Harvest and regrow 3-4 times. Perfect windowsill starter plant — no soil needed!"),

            new Plant("Ginger (Adrak)", "🌱", "Vegetable & Medicinal",
                "Tropical root crop perfect for Indian monsoon. Simply plant a piece of kitchen ginger to start.",
                "Moderate", "Partial Shade", "Easy", "Mo,Au",
                "tropical,coastal",
                "LB,Te,BG", "pMed,pHerb,pVeg",
                "Plant rhizome just below surface in deep pot. Partial shade preferred. Harvest when leaves yellow."),

            new Plant("Turmeric (Haldi)", "🌿", "Medicinal & Vegetable",
                "Golden superfood spice grown from rhizomes. Thrives naturally in Indian tropical monsoon conditions.",
                "Moderate", "Partial Shade", "Easy", "Mo,Au",
                "tropical,coastal",
                "Te,BG", "pMed,pVeg",
                "Plant in deep pots (12+ inches). Harvest after 8-9 months when leaves turn completely yellow."),

            new Plant("Marigold (Genda)", "🌼", "Flower & Ornamental",
                "India's most popular garden flower. Pest repellent, drought tolerant, and blooms continuously.",
                "Low", "Full Sun", "Very Easy", "Au,Wi,Sp",
                "hot_dry,humid_sub,tropical,coastal,highland",
                "SB,LB,Te,BG", "pFlower,pOrnament",
                "Pinch dead flowers daily for non-stop blooms. Plant near vegetables to repel pests organically."),

            new Plant("Rose", "🌹", "Flower & Ornamental",
                "Queen of flowers. Blooms beautifully in Indian winters and spring with proper seasonal pruning.",
                "Moderate", "Full Sun", "Moderate", "Au,Wi,Sp",
                "hot_dry,humid_sub,tropical,coastal,highland",
                "LB,Te,BG", "pFlower,pOrnament",
                "Prune heavily in Oct-Nov for winter bloom. Add banana peel water for bigger, more fragrant flowers."),

            new Plant("Sunflower", "🌻", "Flower & Ornamental",
                "Towering cheerful flower that follows the sun. Perfect statement plant for open terraces.",
                "Low", "Full Sun", "Easy", "Sp,Su",
                "hot_dry,humid_sub,tropical,coastal",
                "LB,Te,BG", "pFlower,pOrnament",
                "Grow dwarf varieties in pots. Needs full sun all day. One plant per 10-litre pot with support stake."),

            new Plant("Hibiscus (Gudhal)", "🌺", "Flower & Medicinal",
                "Vibrant tropical flower that blooms nearly year-round. Used in hair care and herbal teas.",
                "Moderate", "Full Sun", "Easy", "Su,Mo,Au,Sp",
                "tropical,coastal,hot_dry",
                "LB,Te,BG", "pFlower,pMed,pOrnament",
                "Prune after each bloom cycle. Feed potassium-rich fertiliser for maximum flower production."),

            new Plant("Jasmine (Mogra)", "🌸", "Flower & Ornamental",
                "Intensely fragrant Indian flower. Fills evenings with perfume. Perfect for balcony railings.",
                "Moderate", "Full Sun", "Moderate", "Su,Mo,Sp",
                "tropical,coastal,hot_dry",
                "LB,Te,BG", "pFlower,pOrnament",
                "Train on trellis or railing. Feed phosphorus fertiliser 4-6 weeks before flowering season."),

            new Plant("Petunia", "💜", "Flower & Ornamental",
                "Cascading colourful flowers perfect for hanging baskets and balcony railings in cool season.",
                "Moderate", "Full Sun", "Easy", "Wi,Sp",
                "hot_dry,humid_sub,tropical,coastal",
                "SB,LB,Te", "pFlower,pOrnament",
                "Deadhead spent flowers regularly for non-stop blooms. Ideal trailing from balcony railing planters."),

            new Plant("Portulaca", "🌷", "Flower & Ornamental",
                "Summer heat and drought champion. Blooms in blazing sun when everything else wilts — truly remarkable.",
                "Very Low", "Full Sun", "Very Easy", "Su,Mo,Sp",
                "hot_dry,humid_sub,tropical,coastal",
                "SB,LB,Te", "pFlower,pOrnament",
                "Thrives on neglect in scorching sun. Never overwater. Perfect for south-facing hot balconies."),

            new Plant("Chrysanthemum (Shevanti)", "🌸", "Flower & Ornamental",
                "Stunning autumn-winter bloomer in dozens of colours. One of India's most popular seasonal flowers.",
                "Moderate", "Full Sun", "Moderate", "Au,Wi",
                "hot_dry,humid_sub,tropical,coastal,highland",
                "SB,LB,Te", "pFlower,pOrnament",
                "Pinch stems until September then let bloom. Needs 12+ hours of darkness nightly to trigger flowering."),

            new Plant("Strawberry", "🍓", "Fruit",
                "Surprisingly easy in Indian winters! Sweet fruits right from hanging baskets or small pots.",
                "Moderate", "Full Sun", "Moderate", "Wi,Sp",
                "highland,hot_dry,humid_sub",
                "SB,LB,Te", "pFruit",
                "Plant Oct-Nov from nursery runners. Keep moist not waterlogged. Harvest when fully red."),

            new Plant("Lemon (Nimbu)", "🍋", "Fruit & Medicinal",
                "India's most popular container fruit tree. Gives fruit nearly year-round with minimal care.",
                "Low", "Full Sun", "Easy", "Su,Mo,Au,Wi,Sp",
                "hot_dry,tropical,coastal,humid_sub",
                "Te,BG", "pFruit,pMed",
                "Use 20-litre pot minimum. Prune annually after fruiting. Feed citrus fertiliser every 2 months."),

            new Plant("Dwarf Banana", "🍌", "Fruit & Ornamental",
                "Tropical showpiece that actually produces bananas in pots. Makes any terrace look lush.",
                "High", "Full Sun", "Moderate", "Su,Mo,Au",
                "tropical,coastal",
                "Te,BG", "pFruit,pOrnament",
                "Needs 25+ litre deep pot. Keep consistently moist. Remove all side shoots except one sucker."),

            new Plant("Money Plant (Pothos)", "🪴", "Air Purifying",
                "NASA-certified top air purifier. Grows in water, soil or low light. Practically indestructible.",
                "Very Low", "Low Light", "Very Easy", "Su,Mo,Au,Wi,Sp",
                "hot_dry,humid_sub,tropical,coastal,highland",
                "SB,LB,Te,In,BG", "pAir,pOrnament",
                "Trail from shelves or grow upward on a moss stick. Change water weekly if growing in a bottle."),

            new Plant("Peace Lily", "🌷", "Air Purifying",
                "Elegant indoor plant that removes benzene and formaldehyde. Blooms beautiful white flowers indoors.",
                "Moderate", "Low Light", "Easy", "Su,Mo,Au,Wi,Sp",
                "tropical,coastal",
                "SB,LB,In", "pAir,pOrnament",
                "Keep away from direct sun. Droops dramatically when thirsty — a perfect natural watering reminder!"),

            new Plant("Spider Plant", "🌿", "Air Purifying",
                "Tough low-maintenance air purifier that produces baby plants automatically. Perfect for beginners.",
                "Low", "Indirect Light", "Very Easy", "Su,Mo,Au,Wi,Sp",
                "hot_dry,humid_sub,tropical,coastal,highland",
                "SB,LB,In", "pAir,pOrnament",
                "Produces hanging baby plants — clip and root in water to propagate endlessly. Tolerates neglect."),

            new Plant("Snake Plant (Sansevieria)", "🌿", "Air Purifying",
                "Releases oxygen at night — perfect bedrooms plant. Survives weeks of neglect and any light level.",
                "Very Low", "Any Light", "Very Easy", "Su,Mo,Au,Wi,Sp",
                "hot_dry,humid_sub,tropical,coastal,highland",
                "SB,LB,In", "pAir,pOrnament,pMed",
                "Water only when soil is bone dry. One of the most recommended plants for Indian apartments."),

            new Plant("Aloe Vera", "🌵", "Succulent & Medicinal",
                "Multipurpose medicinal succulent. Soothes burns, aids skin care, and thrives completely on neglect.",
                "Very Low", "Full Sun", "Very Easy", "Su,Mo,Au,Wi,Sp",
                "hot_dry,humid_sub,tropical,coastal",
                "SB,LB,Te", "pSucc,pMed,pAir",
                "Water only every 2-3 weeks. Never let it sit in water. Break a leaf open for instant burn relief."),

            new Plant("Jade Plant", "🪴", "Succulent & Ornamental",
                "Low-maintenance succulent that lives for decades. Symbolises good luck and prosperity in Indian homes.",
                "Very Low", "Bright Indirect", "Very Easy", "Su,Mo,Au,Wi,Sp",
                "hot_dry,tropical,humid_sub",
                "SB,LB,Te,In", "pSucc,pOrnament",
                "Water only when topsoil is completely dry. Repot every 2-3 years. Propagates easily from cuttings."),

            new Plant("Echeveria", "🪴", "Succulent & Ornamental",
                "Stunning jewel-coloured rosette succulent. Perfect tiny pot plant for any small urban space.",
                "Very Low", "Bright Light", "Very Easy", "Su,Mo,Au,Wi,Sp",
                "hot_dry,tropical,humid_sub",
                "SB,LB,In", "pSucc,pOrnament",
                "Never let water sit in rosette centre — causes rot. Use well-draining cactus mix soil only."),

            new Plant("Bamboo Palm", "🌴", "Ornamental & Air Purifying",
                "Tropical indoor palm that creates a lush resort-like atmosphere. Excellent air purifier for rooms.",
                "Moderate", "Indirect Light", "Moderate", "Su,Mo,Au,Wi,Sp",
                "tropical,coastal",
                "LB,Te,In", "pAir,pOrnament",
                "Keep moist but not soggy. Mist leaves in dry weather. Bright indirect light gives best results."),

            new Plant("Croton", "🍂", "Ornamental",
                "Striking tropical foliage in red, yellow, green and orange. Bold colour statement all year round.",
                "Moderate", "Bright Indirect", "Moderate", "Su,Mo,Au,Wi,Sp",
                "tropical,coastal",
                "LB,Te,In", "pOrnament",
                "Needs bright light for vivid colours. Avoid cold drafts and sudden temperature changes — it sulks!"),

            new Plant("Rubber Plant (Ficus)", "🌳", "Ornamental & Air Purifying",
                "Bold architectural indoor plant with large glossy leaves. Effectively purifies indoor air.",
                "Low", "Indirect Light", "Easy", "Su,Mo,Au,Wi,Sp",
                "tropical,coastal,hot_dry,humid_sub",
                "LB,Te,In", "pOrnament,pAir",
                "Wipe leaves monthly with damp cloth for max air purification and glossy look. Repot every 2 years."),

            new Plant("Bougainvillea", "🪷", "Ornamental & Flower",
                "India's most spectacular climbing flower. Thrives on heat and drought, blooms for months on end.",
                "Low", "Full Sun", "Easy", "Su,Sp,Au",
                "hot_dry,tropical,coastal,humid_sub",
                "Te,BG", "pOrnament,pFlower",
                "Water stress actually triggers flowering — water less before blooming season. Train on trellis."),

            new Plant("Ashwagandha", "🌿", "Medicinal",
                "Revered Ayurvedic adaptogen that grows like a weed in Indian soil with almost zero care needed.",
                "Low", "Full Sun", "Easy", "Mo,Au",
                "hot_dry,tropical,humid_sub",
                "Te,BG", "pMed",
                "Direct sow seeds post-monsoon. Harvest roots after 150-180 days. Extremely drought tolerant."),

            new Plant("Neem", "🌳", "Medicinal & Ornamental",
                "India's wonder tree — natural pesticide, air purifier and medicinal powerhouse all in one plant.",
                "Very Low", "Full Sun", "Very Easy", "Su,Mo,Au,Wi,Sp",
                "hot_dry,tropical,coastal,humid_sub",
                "Te,BG", "pMed,pOrnament",
                "Use neem leaf spray on other plants as organic pesticide. Grows fast in any Indian soil condition."),

            new Plant("Bamboo (Lucky)", "🎍", "Ornamental",
                "Fast-growing ornamental grass that brings a serene, zen-like quality to any indoor or outdoor space.",
                "Moderate", "Indirect Light", "Easy", "Su,Mo,Au,Wi,Sp",
                "tropical,coastal,humid_sub",
                "LB,Te,In,BG", "pOrnament",
                "Grows well in water or soil. Change water every 2 weeks. Indirect light keeps leaves green and lush."),

            new Plant("Guava (Dwarf Amrood)", "🍏", "Fruit",
                "Prolific Indian fruit tree that produces abundantly in containers. Rich in Vitamin C.",
                "Low", "Full Sun", "Easy", "Su,Mo,Au,Wi,Sp",
                "hot_dry,tropical,coastal,humid_sub",
                "Te,BG", "pFruit",
                "Use a 20-litre pot. Prune to control height. Fruiting starts from year 2. Extremely productive."),

            new Plant("Beetroot (Chukandar)", "🫐", "Vegetable",
                "Dual-purpose root vegetable — harvest nutritious leaves and sweet roots. Easy cool-season crop that thrives in pots.",
                "Moderate", "Full Sun", "Easy", "Au,Wi",
                "hot_dry,humid_sub,tropical,coastal,highland",
                "SB,LB,Te", "pVeg",
                "Use 8-inch deep pots — roots need depth. Sow direct, thin to 4-inch spacing. Leaves ready in 30 days, roots by day 60-70."),

            new Plant("Zinnia", "💐", "Flower & Ornamental",
                "Heat-tolerant summer bloomer that flowers in just 6 weeks. One of the most rewarding balcony flowers — germinates in 5 days.",
                "Low", "Full Sun", "Very Easy", "Su,Mo,Au,Sp",
                "hot_dry,humid_sub,tropical,coastal",
                "SB,LB,Te", "pFlower,pOrnament",
                "Deadhead spent flowers every 3 days for non-stop blooms. Tolerates harsh Indian summer heat."),

            new Plant("Methi Microgreens (Fenugreek)", "🌱", "Herb & Medicinal",
                "Fastest crop possible — harvest nutritious sprouts in 5 days or microgreens in 12 days. No soil needed for sprouting.",
                "Low", "Partial Sun", "Very Easy", "Au,Wi,Sp",
                "hot_dry,humid_sub,tropical,coastal,highland",
                "SB,LB,Te,In", "pHerb,pMed,pVeg",
                "Soak seeds 8 hours, then spread on damp cotton cloth. Rinse twice daily. Sprouts in 5 days."),

            new Plant("Bottle Gourd (Lauki)", "🫙", "Vegetable",
                "India's most beloved summer vegetable. Grows vigorously on a trellis and produces abundantly on terraces and backyards.",
                "High", "Full Sun", "Easy", "Su,Mo",
                "hot_dry,humid_sub,tropical,coastal",
                "Te,BG", "pVeg",
                "Sow 2 seeds per large pot (20+ litres). Train on a strong trellis — vines get very heavy. Harvest young at 20-25 cm."),

            new Plant("Lavender", "🪻", "Medicinal & Ornamental",
                "Fragrant Mediterranean herb that doubles as a stunning ornamental. Repels mosquitoes and moths naturally.",
                "Very Low", "Full Sun", "Easy", "Wi,Sp,Au",
                "hot_dry,highland,humid_sub",
                "SB,LB,Te", "pMed,pOrnament,pFlower",
                "Needs excellent drainage — add 30% perlite to potting mix. Never water on leaves. Harvest stems just before flowers fully open."),

            new Plant("Bitter Gourd (Karela)", "🥝", "Vegetable & Medicinal",
                "Highly medicinal Indian vegetable that manages blood sugar levels. Grows vigorously on vertical trellises in hot weather.",
                "Moderate", "Full Sun", "Easy", "Su,Mo",
                "hot_dry,humid_sub,tropical,coastal",
                "Te,BG", "pVeg,pMed",
                "Train on a trellis or net — climbs aggressively. Harvest fruits when 10-12 cm and still bright green.")
        );

        plantRepository.saveAll(plants);
        System.out.println("✅ Seeded " + plants.size() + " plants into database.");
    }
}
