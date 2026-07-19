package com.travelify.config;

import com.github.javafaker.Faker;
import com.travelify.model.*;
import com.travelify.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Seeds realistic demo data for Module 2: Trip Management.
 * <p>
 * Creates:
 * - 10 trip categories
 * - 20 trip templates (by admin)
 * - 5 travel agents
 * - 10 hotels, 5 insurance agents, 5 visa agents (with provider_type)
 * - ~300 agent trips (5-10 per travel agent from templates)
 * - ~50 hotel services, ~20 insurance plans, ~30 visa services
 * - Random service attachments to agent trips (0-3 per trip)
 * <p>
 * Runs only on dev profile and when data-seeder.enabled=true (default: true in dev).
 * Idempotent: checks if data already exists before seeding.
 * <p>
 * All seeded users have password: password123
 */
@Configuration
@ConditionalOnProperty(name = "data-seeder.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final String DEFAULT_PASSWORD = "password123";
    private static final String IMAGE_BASE_URL = "https://picsum.photos/800/600?random=";

    private final Faker faker = new Faker();

    @Bean
    CommandLineRunner seedDemoData(
            UserRepository userRepository,
            TripCategoryRepository tripCategoryRepository,
            TripTemplateRepository tripTemplateRepository,
            AgentTripRepository agentTripRepository,
            AgentTripItineraryRepository agentTripItineraryRepository,
            AgentTripPricingRepository agentTripPricingRepository,
            AgentTripDepartureRepository agentTripDepartureRepository,
            AgentTripImageRepository agentTripImageRepository,
            ServiceRepository serviceRepository,
            TripServiceRepository tripServiceRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            long startTime = System.currentTimeMillis();
            log.info("Starting DataSeeder...");

            // Check if already seeded
            if (tripCategoryRepository.count() > 0) {
                log.info("DataSeeder skipped — data already exists ({} categories found)", 
                        tripCategoryRepository.count());
                return;
            }

            String hashedPassword = passwordEncoder.encode(DEFAULT_PASSWORD);

            // 1. Create trip categories
            log.info("Creating 10 trip categories...");
            List<TripCategory> categories = createCategories();
            tripCategoryRepository.saveAll(categories);

            // 2. Get or create admin user for templates
            User adminUser = userRepository.findByEmail("admin@travelify.com")
                    .orElseGet(() -> createAdminUser(hashedPassword, userRepository));

            // 3. Create trip templates
            log.info("Creating 20 trip templates...");
            List<TripTemplate> templates = createTemplates(categories, adminUser);
            tripTemplateRepository.saveAll(templates);

            // 4. Create provider users (travel agents, hotels, insurance, visa)
            log.info("Creating provider users...");
            List<User> travelAgents = createTravelAgents(hashedPassword);
            List<User> hotelProviders = createHotelProviders(hashedPassword);
            List<User> insuranceProviders = createInsuranceProviders(hashedPassword);
            List<User> visaProviders = createVisaProviders(hashedPassword);
            
            // Save all users, checking for existence first
            List<User> allProviders = new ArrayList<>();
            allProviders.addAll(travelAgents);
            allProviders.addAll(hotelProviders);
            allProviders.addAll(insuranceProviders);
            allProviders.addAll(visaProviders);

            List<User> newUsersToSave = new ArrayList<>();
            for (User user : allProviders) {
                if (userRepository.findByEmail(user.getEmail()).isEmpty()) {
                    newUsersToSave.add(user);
                }
            }
            userRepository.saveAll(newUsersToSave);

            // Re-fetch users to ensure all are managed entities
            travelAgents = travelAgents.stream()
                    .map(user -> userRepository.findByEmail(user.getEmail()).orElse(user))
                    .collect(Collectors.toList());
            hotelProviders = hotelProviders.stream()
                    .map(user -> userRepository.findByEmail(user.getEmail()).orElse(user))
                    .collect(Collectors.toList());
            insuranceProviders = insuranceProviders.stream()
                    .map(user -> userRepository.findByEmail(user.getEmail()).orElse(user))
                    .collect(Collectors.toList());
            visaProviders = visaProviders.stream()
                    .map(user -> userRepository.findByEmail(user.getEmail()).orElse(user))
                    .collect(Collectors.toList());
            
            // 5. Create services for each provider
            log.info("Creating services...");
            List<Service> hotelServices = createHotelServices(hotelProviders);
            List<Service> insuranceServices = createInsuranceServices(insuranceProviders);
            List<Service> visaServices = createVisaServices(visaProviders);
            
            List<Service> allServices = new ArrayList<>();
            allServices.addAll(hotelServices);
            allServices.addAll(insuranceServices);
            allServices.addAll(visaServices);
            serviceRepository.saveAll(allServices);

            // 6. Create agent trips from templates
            log.info("Creating ~300 agent trips...");
            List<AgentTrip> allAgentTrips = new ArrayList<>();
            
            for (User travelAgent : travelAgents) {
                int tripsPerAgent = faker.number().numberBetween(5, 10);
                List<AgentTrip> agentTrips = createAgentTrips(templates, travelAgent, tripsPerAgent);
                allAgentTrips.addAll(agentTrips);
            }
            
            // Save agent trips
            agentTripRepository.saveAll(allAgentTrips);

            // 7. Create child entities for agent trips (itinerary, pricing, departures, images)
            log.info("Creating agent trip details (itinerary, pricing, departures, images)...");
            List<AgentTripItinerary> allItineraries = new ArrayList<>();
            List<AgentTripPricing> allPricings = new ArrayList<>();
            List<AgentTripDeparture> allDepartures = new ArrayList<>();
            List<AgentTripImage> allImages = new ArrayList<>();
            
            for (AgentTrip trip : allAgentTrips) {
                // Create itinerary days
                int days = trip.getDurationDays() != null ? trip.getDurationDays() : faker.number().numberBetween(3, 14);
                List<AgentTripItinerary> itineraries = createItineraryDays(trip, days);
                allItineraries.addAll(itineraries);
                
                // Create pricing
                List<AgentTripPricing> pricings = createPricingForTrip(trip);
                allPricings.addAll(pricings);
                
                // Create departures
                int departureCount = faker.number().numberBetween(1, 3);
                List<AgentTripDeparture> departures = createDepartures(trip, days, departureCount);
                allDepartures.addAll(departures);
                
                // Create images
                int imageCount = faker.number().numberBetween(1, 5);
                List<AgentTripImage> images = createImages(trip, imageCount);
                allImages.addAll(images);
            }
            
            agentTripItineraryRepository.saveAll(allItineraries);
            agentTripPricingRepository.saveAll(allPricings);
            agentTripDepartureRepository.saveAll(allDepartures);
            agentTripImageRepository.saveAll(allImages);

            // 8. Attach services to agent trips
            log.info("Attaching services to agent trips...");
            List<TripService> tripServices = new ArrayList<>();
            
            for (AgentTrip trip : allAgentTrips) {
                int serviceCount = faker.number().numberBetween(0, 3);
                List<Service> selectedServices = getRandomServices(allServices, serviceCount);
                
                for (Service service : selectedServices) {
                    TripService tripService = TripService.builder()
                            .agentTrip(trip)
                            .service(service)
                            .isOptional(true)
                            .createdAt(Instant.now())
                            .build();
                    tripServices.add(tripService);
                }
            }
            
            tripServiceRepository.saveAll(tripServices);

            // 9. Publish some data
            log.info("Publishing some data...");
            publishRandomData(templates, allAgentTrips, allServices);

            long duration = System.currentTimeMillis() - startTime;
            log.info("DataSeeder completed in {}ms", duration);
            log.info("Seeded: {} categories, {} templates, {} agent trips, {} services, {} trip-services",
                    categories.size(), templates.size(), allAgentTrips.size(), allServices.size(), tripServices.size());
        };
    }

    // ==================== CATEGORY CREATION ====================

    private List<TripCategory> createCategories() {
        List<String[]> categoryData = List.of(
                new String[]{"Adventure", "adventure", "Heart-pumping adventures and expeditions"},
                new String[]{"Beach", "beach", "Relax on the world's most beautiful beaches"},
                new String[]{"Cultural", "cultural", "Immerse yourself in local cultures and history"},
                new String[]{"Family", "family", "Perfect trips for the whole family"},
                new String[]{"Honeymoon", "honeymoon", "Romantic getaways for couples"},
                new String[]{"Luxury", "luxury", "Premium travel experiences"},
                new String[]{"Nature", "nature", "Explore natural wonders and wildlife"},
                new String[]{"Ski & Snow", "ski-snow", "Winter sports and mountain resorts"},
                new String[]{"Wildlife", "wildlife", "Safaris and wildlife encounters"},
                new String[]{"Wellness", "wellness", "Spa, yoga, and relaxation retreats"}
        );

        return IntStream.range(0, categoryData.size())
                .mapToObj(i -> {
                    String[] data = categoryData.get(i);
                    return TripCategory.builder()
                            .name(data[0])
                            .slug(data[1])
                            .description(data[2])
                            .icon(getIconForCategory(data[1]))
                            .sortOrder(i)
                            .isActive(true)
                            .createdAt(Instant.now())
                            .updatedAt(Instant.now())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private String getIconForCategory(String slug) {
        Map<String, String> icons = Map.of(
                "adventure", "🏔️",
                "beach", "🏖️",
                "cultural", "🏛️",
                "family", "👨‍👩‍👧‍👦",
                "honeymoon", "💑",
                "luxury", "💎",
                "nature", "🌿",
                "ski-snow", "⛷️",
                "wildlife", "🦁",
                "wellness", "🧘"
        );
        return icons.getOrDefault(slug, "🌍");
    }

    // ==================== USER CREATION ====================

    private User createAdminUser(String hashedPassword, UserRepository userRepository) {
        User admin = User.builder()
                .email("admin@travelify.com")
                .password(hashedPassword)
                .firstName("Travelify")
                .lastName("Admin")
                .phone("+1-555-0101")
                .role(Role.ADMIN)
                .isActive(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        return userRepository.save(admin);
    }

    private List<User> createTravelAgents(String hashedPassword) {
        List<String[]> agentData = List.of(
                new String[]{"agent1@travelify.com", "Alex", "Johnson"},
                new String[]{"agent2@travelify.com", "Sara", "Chen"},
                new String[]{"agent3@travelify.com", "Marcus", "Nguyen"},
                new String[]{"agent4@travelify.com", "Priya", "Patel"},
                new String[]{"agent5@travelify.com", "Diego", "Ramirez"}
        );

        return IntStream.range(0, agentData.size())
                .mapToObj(i -> {
                    String[] data = agentData.get(i);
                    return User.builder()
                            .email(data[0])
                            .password(hashedPassword)
                            .firstName(data[1])
                            .lastName(data[2])
                            .phone("+1-555-020" + (i + 1))
                            .role(Role.AGENT)
                            .providerType(ProviderType.TRAVEL_AGENT)
                            .businessName(data[1] + " Travel Agency")
                            .businessAddress(faker.address().fullAddress())
                            .isActive(true)
                            .createdAt(Instant.now())
                            .updatedAt(Instant.now())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<User> createHotelProviders(String hashedPassword) {
        return IntStream.range(1, 11)
                .mapToObj(i -> User.builder()
                        .email("hotel" + i + "@travelify.com")
                        .password(hashedPassword)
                        .firstName(faker.name().firstName())
                        .lastName(faker.name().lastName())
                        .phone("+1-555-1" + String.format("%03d", i))
                        .role(Role.AGENT)
                        .providerType(ProviderType.HOTEL)
                        .businessName(faker.company().name() + " Hotel")
                        .businessAddress(faker.address().fullAddress())
                        .isActive(true)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build())
                .collect(Collectors.toList());
    }

    private List<User> createInsuranceProviders(String hashedPassword) {
        return IntStream.range(1, 6)
                .mapToObj(i -> User.builder()
                        .email("insurance" + i + "@travelify.com")
                        .password(hashedPassword)
                        .firstName(faker.name().firstName())
                        .lastName(faker.name().lastName())
                        .phone("+1-555-2" + String.format("%03d", i))
                        .role(Role.AGENT)
                        .providerType(ProviderType.INSURANCE)
                        .businessName(faker.company().name() + " Insurance")
                        .businessAddress(faker.address().fullAddress())
                        .isActive(true)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build())
                .collect(Collectors.toList());
    }

    private List<User> createVisaProviders(String hashedPassword) {
        return IntStream.range(1, 6)
                .mapToObj(i -> User.builder()
                        .email("visa" + i + "@travelify.com")
                        .password(hashedPassword)
                        .firstName(faker.name().firstName())
                        .lastName(faker.name().lastName())
                        .phone("+1-555-3" + String.format("%03d", i))
                        .role(Role.AGENT)
                        .providerType(ProviderType.VISA)
                        .businessName(faker.company().name() + " Visa Services")
                        .businessAddress(faker.address().fullAddress())
                        .isActive(true)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build())
                .collect(Collectors.toList());
    }

    // ==================== TEMPLATE CREATION ====================

    private List<TripTemplate> createTemplates(List<TripCategory> categories, User adminUser) {
        List<String[]> templateDestinations = List.of(
                new String[]{"Bali Adventure", "bali-adventure", "Explore the tropical paradise of Bali"},
                new String[]{"Paris Romance", "paris-romance", "Discover the city of love"},
                new String[]{"Tokyo Discovery", "tokyo-discovery", "Experience modern and traditional Japan"},
                new String[]{"Santorini Sunset", "santorini-sunset", "Witness breathtaking sunsets in Greece"},
                new String[]{"Patagonia Trek", "patagonia-trek", "Hike through stunning landscapes"},
                new String[]{"New York City", "new-york-city", "The city that never sleeps"},
                new String[]{"Machu Picchu", "machu-picchu", "Ancient Inca trail adventure"},
                new String[]{"Amalfi Coast", "amalfi-coast", "Italian coastal beauty"},
                new String[]{"Safari Africa", "safari-africa", "Wildlife expedition"},
                new String[]{"Australian Outback", "australian-outback", "Red desert adventures"},
                new String[]{"Canadian Rockies", "canadian-rockies", "Mountain landscapes"},
                new String[]{"Thailand Beaches", "thailand-beaches", "Tropical island hopping"},
                new String[]{"Morocco Culture", "morocco-culture", "Exotic North African experience"},
                new String[]{"Alaska Cruise", "alaska-cruise", "Glacier and wildlife cruise"},
                new String[]{"Swiss Alps", "swiss-alps", "Snow-capped mountain retreats"},
                new String[]{"Egypt Pyramids", "egypt-pyramids", "Ancient wonders tour"},
                new String[]{"Costa Rica Eco", "costa-rica-eco", "Eco-tourism and wildlife"},
                new String[]{"Iceland Northern Lights", "iceland-northern-lights", "Aurora borealis chase"},
                new String[]{"Dubai Luxury", "dubai-luxury", "Ultra-modern luxury experience"},
                new String[]{"Vietnam Discovery", "vietnam-discovery", "Cultural journey through Vietnam"}
        );

        return IntStream.range(0, templateDestinations.size())
                .mapToObj(i -> {
                    String[] data = templateDestinations.get(i);
                    TripCategory category = categories.get(faker.number().numberBetween(0, categories.size() - 1));
                    
                    return TripTemplate.builder()
                            .title(data[0])
                            .slug(data[1])
                            .shortDescription(data[2])
                            .fullDescription(generateTemplateDescription(data[0]))
                            .featuredImage(IMAGE_BASE_URL + i)
                            .category(category)
                            .difficulty(getRandomDifficulty())
                            .durationDays(faker.number().numberBetween(3, 14))
                            .minAge(faker.number().numberBetween(0, 18))
                            .maxGroupSize(faker.number().numberBetween(2, 50))
                            .isFeatured(i < 5) // First 5 are featured
                            .status(PublishStatus.PUBLISHED)
                            .createdBy(adminUser)
                            .createdAt(Instant.now())
                            .updatedAt(Instant.now())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private String generateTemplateDescription(String title) {
        return faker.lorem().paragraph() + " " + faker.lorem().paragraph();
    }

    private String getRandomDifficulty() {
        String[] difficulties = {"Easy", "Moderate", "Challenging", "Difficult", "Expert"};
        return difficulties[faker.number().numberBetween(0, difficulties.length - 1)];
    }

    // ==================== AGENT TRIP CREATION ====================

    private List<AgentTrip> createAgentTrips(List<TripTemplate> templates, User travelAgent, int count) {
        Set<Long> usedTemplateIds = new HashSet<>();
        List<AgentTrip> trips = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            TripTemplate template = templates.get(faker.number().numberBetween(0, templates.size() - 1));
            
            // Create variation
            String suffix = " - " + faker.address().country();
            String title = template.getTitle() + suffix;
            String slug = (template.getSlug() + "-" + faker.code().isbn10().replace("-", "").substring(0, 8)).toLowerCase();

            AgentTrip trip = AgentTrip.builder()
                    .template(template)
                    .agent(travelAgent)
                    .title(title)
                    .slug(slug)
                    .shortDescription(template.getShortDescription() + " with " + travelAgent.getBusinessName())
                    .fullDescription(template.getFullDescription())
                    .featuredImage(IMAGE_BASE_URL + faker.number().numberBetween(100, 999))
                    .category(template.getCategory())
                    .difficulty(template.getDifficulty())
                    .durationDays(template.getDurationDays())
                    .minAge(template.getMinAge())
                    .maxGroupSize(template.getMaxGroupSize())
                    .isFeatured(faker.bool().bool())
                    .status(PublishStatus.DRAFT)
                    .overrideFields(new HashMap<>())
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            trips.add(trip);
        }

        return trips;
    }

    // ==================== SERVICE CREATION ====================

    private List<Service> createHotelServices(List<User> hotelProviders) {
        List<Service> services = new ArrayList<>();
        
        for (User provider : hotelProviders) {
            int serviceCount = faker.number().numberBetween(4, 6);
            for (int i = 0; i < serviceCount; i++) {
                Service service = Service.builder()
                        .provider(provider)
                        .serviceType(ServiceType.HOTEL_ROOM)
                        .name(provider.getBusinessName() + " " + getRandomRoomType())
                        .description(faker.lorem().sentence())
                        .price(new BigDecimal(faker.number().numberBetween(50, 500)))
                        .currency("USD")
                        .meta(createHotelMeta())
                        .status(PublishStatus.PUBLISHED)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build();
                services.add(service);
            }
        }
        
        return services;
    }

    private List<Service> createInsuranceServices(List<User> insuranceProviders) {
        List<Service> services = new ArrayList<>();
        
        for (User provider : insuranceProviders) {
            int serviceCount = faker.number().numberBetween(3, 5);
            for (int i = 0; i < serviceCount; i++) {
                Service service = Service.builder()
                        .provider(provider)
                        .serviceType(ServiceType.INSURANCE_PLAN)
                        .name(provider.getBusinessName() + " " + getRandomInsurancePlan())
                        .description(faker.lorem().sentence())
                        .price(new BigDecimal(faker.number().numberBetween(20, 200)))
                        .currency("USD")
                        .meta(createInsuranceMeta())
                        .status(PublishStatus.PUBLISHED)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build();
                services.add(service);
            }
        }
        
        return services;
    }

    private List<Service> createVisaServices(List<User> visaProviders) {
        List<Service> services = new ArrayList<>();
        
        for (User provider : visaProviders) {
            int serviceCount = faker.number().numberBetween(5, 7);
            for (int i = 0; i < serviceCount; i++) {
                Service service = Service.builder()
                        .provider(provider)
                        .serviceType(ServiceType.VISA_SERVICE)
                        .name(provider.getBusinessName() + " " + getRandomVisaType())
                        .description(faker.lorem().sentence())
                        .price(new BigDecimal(faker.number().numberBetween(50, 300)))
                        .currency("USD")
                        .meta(createVisaMeta())
                        .status(PublishStatus.PUBLISHED)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build();
                services.add(service);
            }
        }
        
        return services;
    }

    private String getRandomRoomType() {
        String[] types = {"Standard Room", "Deluxe Room", "Suite", "Family Room", "Ocean View", 
                "Mountain View", "Honeymoon Suite", "Executive Room", "Presidential Suite"};
        return types[faker.number().numberBetween(0, types.length - 1)];
    }

    private String getRandomInsurancePlan() {
        String[] plans = {"Basic Travel", "Comprehensive", "Medical Only", "Trip Cancellation", 
                "Adventure Sports", "Family Plan", "Senior Travel", "Business Travel"};
        return plans[faker.number().numberBetween(0, plans.length - 1)];
    }

    private String getRandomVisaType() {
        String[] types = {"Tourist Visa", "Business Visa", "Student Visa", "Work Visa", 
                "Transit Visa", "Multiple Entry", "E-Visa", "Express Visa"};
        return types[faker.number().numberBetween(0, types.length - 1)];
    }

    private Map<String, Object> createHotelMeta() {
        Map<String, Object> meta = new HashMap<>();
        meta.put("bedType", faker.number().numberBetween(1, 3));
        meta.put("maxOccupancy", faker.number().numberBetween(1, 4));
        meta.put("amenities", List.of("WiFi", "Breakfast", "Air Conditioning", "TV"));
        meta.put("roomSize", faker.number().numberBetween(20, 100) + " sqm");
        return meta;
    }

    private Map<String, Object> createInsuranceMeta() {
        Map<String, Object> meta = new HashMap<>();
        meta.put("coverageAmount", faker.number().numberBetween(10000, 1000000));
        meta.put("durationDays", faker.number().numberBetween(7, 180));
        meta.put("coverageType", List.of("Medical", "Trip Cancellation", "Baggage", "Emergency Evacuation"));
        meta.put("ageLimit", faker.number().numberBetween(18, 80));
        return meta;
    }

    private Map<String, Object> createVisaMeta() {
        Map<String, Object> meta = new HashMap<>();
        meta.put("processingTime", faker.number().numberBetween(3, 30) + " days");
        meta.put("validity", faker.number().numberBetween(30, 365) + " days");
        meta.put("entryType", faker.bool().bool() ? "Single" : "Multiple");
        meta.put("requiredDocuments", List.of("Passport", "Photo", "Application Form"));
        return meta;
    }

    // ==================== CHILD ENTITIES CREATION ====================

    private List<AgentTripItinerary> createItineraryDays(AgentTrip trip, int days) {
        return IntStream.range(1, days + 1)
                .mapToObj(day -> AgentTripItinerary.builder()
                        .agentTrip(trip)
                        .dayNumber(day)
                        .title("Day " + day + ": " + faker.address().city())
                        .description(faker.lorem().sentence())
                        .activities(faker.lorem().sentence())
                        .accommodation(faker.address().fullAddress())
                        .meals(getRandomMeals())
                        .build())
                .collect(Collectors.toList());
    }

    private String getRandomMeals() {
        String[] meals = {"Breakfast", "Breakfast, Lunch", "Breakfast, Lunch, Dinner", 
                "All meals included", "No meals"};
        return meals[faker.number().numberBetween(0, meals.length - 1)];
    }

    private List<AgentTripPricing> createPricingForTrip(AgentTrip trip) {
        List<AgentTripPricing> pricings = new ArrayList<>();
        
        // Main pricing
        PricingType[] types = {PricingType.PER_PERSON, PricingType.PER_GROUP, PricingType.FIXED};
        for (PricingType type : types) {
            if (faker.bool().bool()) {
                AgentTripPricing pricing = AgentTripPricing.builder()
                        .agentTrip(trip)
                        .pricingType(type)
                        .price(new BigDecimal(faker.number().numberBetween(500, 5000)))
                        .currency("USD")
                        .adultPrice(type == PricingType.PER_PERSON ? new BigDecimal(faker.number().numberBetween(500, 5000)) : null)
                        .childPrice(type == PricingType.PER_PERSON ? new BigDecimal(faker.number().numberBetween(250, 2500)) : null)
                        .infantPrice(type == PricingType.PER_PERSON ? new BigDecimal(faker.number().numberBetween(100, 1000)) : null)
                        .minParticipants(type == PricingType.PER_GROUP ? faker.number().numberBetween(2, 10) : null)
                        .maxParticipants(type == PricingType.PER_GROUP ? faker.number().numberBetween(10, 50) : null)
                        .build();
                pricings.add(pricing);
            }
        }
        
        return pricings;
    }

    private List<AgentTripDeparture> createDepartures(AgentTrip trip, int durationDays, int count) {
        LocalDate today = LocalDate.now();
        return IntStream.range(0, count)
                .mapToObj(i -> {
                    LocalDate departureDate = today.plusDays(faker.number().numberBetween(7, 90));
                    LocalDate endDate = departureDate.plusDays(durationDays);
                    
                    return AgentTripDeparture.builder()
                            .agentTrip(trip)
                            .departureDate(departureDate)
                            .endDate(endDate)
                            .availableSeats(faker.number().numberBetween(2, 50))
                            .priceOverride(faker.bool().bool() ? new BigDecimal(faker.number().numberBetween(50, 500)) : null)
                            .isCancelled(false)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<AgentTripImage> createImages(AgentTrip trip, int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> AgentTripImage.builder()
                        .agentTrip(trip)
                        .imageUrl(IMAGE_BASE_URL + faker.number().numberBetween(1000, 9999))
                        .isFeatured(i == 0)
                        .sortOrder(i)
                        .build())
                .collect(Collectors.toList());
    }

    // ==================== SERVICE ATTACHMENT ====================

    private List<Service> getRandomServices(List<Service> allServices, int count) {
        if (allServices.isEmpty() || count == 0) {
            return List.of();
        }
        
        Collections.shuffle(allServices);
        return allServices.stream()
                .limit(Math.min(count, allServices.size()))
                .collect(Collectors.toList());
    }

    // ==================== PUBLISH DATA ====================

    private void publishRandomData(List<TripTemplate> templates, List<AgentTrip> agentTrips, List<Service> services) {
        // Publish 80% of templates
        templates.forEach(template -> {
            if (faker.bool().bool()) {
                template.setStatus(PublishStatus.PUBLISHED);
            }
        });

        // Publish 70% of agent trips
        agentTrips.forEach(trip -> {
            if (faker.bool().bool()) {
                trip.setStatus(PublishStatus.PUBLISHED);
            }
        });

        // Publish all services (already published in creation)
    }
}