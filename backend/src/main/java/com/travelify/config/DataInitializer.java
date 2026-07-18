package com.travelify.config;

import com.travelify.model.Role;
import com.travelify.model.TravelPackage;
import com.travelify.model.User;
import com.travelify.repository.TravelPackageRepository;
import com.travelify.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Seeds ~30 users (CUSTOMER / AGENT / ADMIN mix) plus sample travel packages
 * when the database is empty.
 * <p>
 * Shared password for all seeded accounts: {@code password123}
 */
@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private static final String DEFAULT_PASSWORD = "password123";

    private static final String[] FIRST_NAMES = {
            "Ava", "Noah", "Mia", "Liam", "Olivia", "Ethan", "Sophia", "Mason",
            "Isabella", "Lucas", "Amelia", "Logan", "Harper", "James", "Evelyn",
            "Benjamin", "Charlotte", "Henry", "Scarlett", "Jack", "Grace", "Owen",
            "Chloe", "Leo", "Zoe", "Caleb", "Lily", "Nathan", "Ella", "Ryan"
    };

    private static final String[] LAST_NAMES = {
            "Johnson", "Smith", "Williams", "Brown", "Jones", "Garcia", "Miller",
            "Davis", "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez",
            "Wilson", "Anderson", "Thomas", "Taylor", "Moore", "Jackson", "Martin",
            "Lee", "Perez", "Thompson", "White", "Harris", "Sanchez", "Clark",
            "Ramirez", "Lewis", "Robinson"
    };

    @Bean
    CommandLineRunner initializeData(UserRepository userRepository,
                                     TravelPackageRepository packageRepository,
                                     PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() > 0) {
                log.info("DataInitializer skipped — users already present ({})", userRepository.count());
                return;
            }

            String hashedPassword = passwordEncoder.encode(DEFAULT_PASSWORD);
            List<User> users = buildUsers(hashedPassword);
            userRepository.saveAll(users);

            long customers = users.stream().filter(u -> u.getRole() == Role.CUSTOMER).count();
            long agents = users.stream().filter(u -> u.getRole() == Role.AGENT).count();
            long admins = users.stream().filter(u -> u.getRole() == Role.ADMIN).count();

            User primaryAgent = users.stream()
                    .filter(u -> u.getRole() == Role.AGENT)
                    .findFirst()
                    .orElse(users.get(0));
            User primaryAdmin = users.stream()
                    .filter(u -> u.getRole() == Role.ADMIN)
                    .findFirst()
                    .orElse(users.get(0));

            seedPackages(packageRepository, primaryAgent, primaryAdmin);

            log.info(
                    "DataInitializer seeded {} users ({} customers, {} agents, {} admins) and sample packages. Password: {}",
                    users.size(), customers, agents, admins, DEFAULT_PASSWORD
            );
        };
    }

    /**
     * 30 users total: 3 ADMIN, 8 AGENT, 19 CUSTOMER.
     * Includes stable demo emails used by the webapp login defaults.
     */
    private List<User> buildUsers(String hashedPassword) {
        List<User> users = new ArrayList<>(30);

        // Stable demo accounts (kept for docs / default login forms)
        users.add(user("admin@travelify.com", "Travelify", "Admin", "+1-555-0101", Role.ADMIN, hashedPassword));
        users.add(user("admin2@travelify.com", "Sara", "Chen", "+1-555-0102", Role.ADMIN, hashedPassword));
        users.add(user("admin3@travelify.com", "Marcus", "Nguyen", "+1-555-0103", Role.ADMIN, hashedPassword));

        users.add(user("agent@travelify.com", "Travel", "Agent", "+1-555-0201", Role.AGENT, hashedPassword));
        users.add(user("agent2@travelify.com", "Priya", "Patel", "+1-555-0202", Role.AGENT, hashedPassword));
        users.add(user("agent3@travelify.com", "Diego", "Ramirez", "+1-555-0203", Role.AGENT, hashedPassword));
        users.add(user("agent4@travelify.com", "Emily", "Brooks", "+1-555-0204", Role.AGENT, hashedPassword));
        users.add(user("agent5@travelify.com", "Kenji", "Tanaka", "+1-555-0205", Role.AGENT, hashedPassword));
        users.add(user("agent6@travelify.com", "Fatima", "Hassan", "+1-555-0206", Role.AGENT, hashedPassword));
        users.add(user("agent7@travelify.com", "Owen", "Clarke", "+1-555-0207", Role.AGENT, hashedPassword));
        users.add(user("agent8@travelify.com", "Nora", "Andersen", "+1-555-0208", Role.AGENT, hashedPassword));

        users.add(user("customer@travelify.com", "Demo", "Customer", "+1-555-0301", Role.CUSTOMER, hashedPassword));

        // Remaining customers to reach 30 (19 customers total including demo)
        int customerIndex = 2;
        for (int i = 0; users.size() < 30; i++) {
            String first = FIRST_NAMES[i % FIRST_NAMES.length];
            String last = LAST_NAMES[(i * 3) % LAST_NAMES.length];
            String email = "customer" + customerIndex + "@travelify.com";
            String phone = String.format("+1-555-%04d", 300 + customerIndex);
            users.add(user(email, first, last, phone, Role.CUSTOMER, hashedPassword));
            customerIndex++;
        }

        return users;
    }

    private User user(String email, String firstName, String lastName, String phone,
                      Role role, String hashedPassword) {
        return User.builder()
                .email(email)
                .password(hashedPassword)
                .firstName(firstName)
                .lastName(lastName)
                .phone(phone)
                .role(role)
                .isActive(true)
                .avatarUrl(null)
                .build();
    }

    private void seedPackages(TravelPackageRepository packageRepository, User agent, User admin) {
        packageRepository.saveAll(List.of(
                TravelPackage.builder()
                        .title("Bali Escape")
                        .description("7-day beach and temple tour across Bali.")
                        .destination("Bali, Indonesia")
                        .price(new BigDecimal("1299.00"))
                        .durationDays(7)
                        .active(true)
                        .createdBy(agent)
                        .build(),
                TravelPackage.builder()
                        .title("Paris Romance")
                        .description("5-day city break with museum passes and Seine cruise.")
                        .destination("Paris, France")
                        .price(new BigDecimal("1599.00"))
                        .durationDays(5)
                        .active(true)
                        .createdBy(admin)
                        .build(),
                TravelPackage.builder()
                        .title("Tokyo Discovery")
                        .description("8-day culture, food, and tech itinerary.")
                        .destination("Tokyo, Japan")
                        .price(new BigDecimal("1899.00"))
                        .durationDays(8)
                        .active(true)
                        .createdBy(agent)
                        .build(),
                TravelPackage.builder()
                        .title("Santorini Sunset")
                        .description("6-day Aegean island hop with cliffside stays.")
                        .destination("Santorini, Greece")
                        .price(new BigDecimal("1749.00"))
                        .durationDays(6)
                        .active(true)
                        .createdBy(agent)
                        .build(),
                TravelPackage.builder()
                        .title("Patagonia Trek")
                        .description("10-day guided hiking adventure in Torres del Paine.")
                        .destination("Patagonia, Chile")
                        .price(new BigDecimal("2499.00"))
                        .durationDays(10)
                        .active(true)
                        .createdBy(admin)
                        .build()
        ));
    }
}
