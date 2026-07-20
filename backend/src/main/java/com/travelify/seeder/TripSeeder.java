package com.travelify.seeder;

import com.travelify.model.TravelPackage;
import com.travelify.model.TripCategory;
import com.travelify.model.User;
import com.travelify.repository.TravelPackageRepository;
import com.travelify.repository.TripCategoryRepository;
import com.travelify.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class TripSeeder implements CommandLineRunner {

    private final TravelPackageRepository travelPackageRepository;
    private final UserRepository userRepository;
    private final TripCategoryRepository tripCategoryRepository;

    public TripSeeder(TravelPackageRepository travelPackageRepository, UserRepository userRepository, TripCategoryRepository tripCategoryRepository) {
        this.travelPackageRepository = travelPackageRepository;
        this.userRepository = userRepository;
        this.tripCategoryRepository = tripCategoryRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (tripCategoryRepository.count() == 0) {
            seedTripCategories();
        }
        if (travelPackageRepository.count() == 0) {
            seedTravelPackages();
        }
    }

    private void seedTripCategories() {
        List<TripCategory> categories = Stream.of(
                TripCategory.builder().name("Adventure").slug("adventure").description("Thrilling adventure trips").isActive(true).sortOrder(1).createdAt(Instant.now()).updatedAt(Instant.now()).build(),
                TripCategory.builder().name("Beach").slug("beach").description("Relaxing beach getaways").isActive(true).sortOrder(2).createdAt(Instant.now()).updatedAt(Instant.now()).build(),
                TripCategory.builder().name("Cultural").slug("cultural").description("Immersive cultural experiences").isActive(true).sortOrder(3).createdAt(Instant.now()).updatedAt(Instant.now()).build(),
                TripCategory.builder().name("City Break").slug("city-break").description("Exciting city explorations").isActive(true).sortOrder(4).createdAt(Instant.now()).updatedAt(Instant.now()).build(),
                TripCategory.builder().name("Nature").slug("nature").description("Discover the beauty of nature").isActive(true).sortOrder(5).createdAt(Instant.now()).updatedAt(Instant.now()).build()
        ).collect(Collectors.toList());
        tripCategoryRepository.saveAll(categories);
        System.out.println("Seeded " + categories.size() + " trip categories.");
    }

    private void seedTravelPackages() {
        Optional<User> adminUserOptional = userRepository.findById(1L); // Assuming user with ID 1 exists
        User adminUser = adminUserOptional.orElseGet(() -> {
            // Create a default user if not found
            User defaultUser = new User();
            defaultUser.setFirstName("admin");
            defaultUser.setLastName("admin");
            defaultUser.setEmail("admin@travelify.com");
            defaultUser.setPassword("password"); // This should be encoded in a real app
            defaultUser.setCreatedAt(Instant.now());
            return userRepository.save(defaultUser);
        });

        List<TripCategory> categories = tripCategoryRepository.findAll();
        if (categories.isEmpty()) {
            System.err.println("No trip categories found. Cannot seed travel packages.");
            return;
        }

        List<TravelPackage> travelPackages = new ArrayList<>();
        Random random = new Random();

        String[] destinations = {"Paris", "Tokyo", "New York", "London", "Rome", "Dubai", "Sydney", "Rio de Janeiro", "Cairo", "Beijing"};
        String[] descriptions = {
                "An unforgettable journey through the city of lights.",
                "Experience the vibrant culture and bustling streets.",
                "Discover iconic landmarks and hidden gems.",
                "A perfect blend of history and modernity.",
                "Indulge in exquisite cuisine and breathtaking views.",
                "Adventure awaits in this exotic paradise.",
                "Explore ancient wonders and futuristic marvels.",
                "Relax on pristine beaches or hike majestic mountains.",
                "Immerse yourself in art, music, and history.",
                "A truly magical escape from the everyday."
        };

        for (int i = 1; i <= 300; i++) {
            String title = "Adventure Package " + i;
            String destination = destinations[random.nextInt(destinations.length)];
            String description = descriptions[random.nextInt(descriptions.length)];
            BigDecimal price = BigDecimal.valueOf(1000 + random.nextInt(5000));
            Integer durationDays = 3 + random.nextInt(10); // 3 to 12 days
            Boolean active = random.nextBoolean();
            TripCategory randomCategory = categories.get(random.nextInt(categories.size()));

            TravelPackage travelPackage = TravelPackage.builder()
                    .title(title)
                    .description(description)
                    .destination(destination)
                    .price(price)
                    .durationDays(durationDays)
                    .active(active)
                    .createdBy(adminUser)
                    .tripCategory(randomCategory) // Assign a random category
                    .createdAt(Instant.now())
                    .build();
            travelPackages.add(travelPackage);
        }
        travelPackageRepository.saveAll(travelPackages);
        System.out.println("Seeded " + travelPackages.size() + " travel packages.");
    }
}
