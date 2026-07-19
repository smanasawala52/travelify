package com.travelify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelify.model.User;
import com.travelify.repository.UserRepository;
import com.travelify.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Base class for integration tests.
 * Provides common setup, test data loading, and utility methods.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = {"/data/test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public abstract class IntegrationTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected JwtUtil jwtUtil;

    protected String adminToken;
    protected String agentToken;
    protected String hotelProviderToken;
    protected String insuranceProviderToken;
    protected String visaProviderToken;
    protected String customerToken;

    protected String getTokenForUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
        return jwtUtil.generateAccessToken(user);
    }

    protected String getAdminToken() {
        if (adminToken == null) {
            adminToken = getTokenForUser("admin@travelify.com");
        }
        return adminToken;
    }

    protected String getAgentToken() {
        if (agentToken == null) {
            agentToken = getTokenForUser("agent1@travelify.com");
        }
        return agentToken;
    }

    protected String getHotelProviderToken() {
        if (hotelProviderToken == null) {
            hotelProviderToken = getTokenForUser("hotel1@travelify.com");
        }
        return hotelProviderToken;
    }

    protected String getInsuranceProviderToken() {
        if (insuranceProviderToken == null) {
            insuranceProviderToken = getTokenForUser("insurance1@travelify.com");
        }
        return insuranceProviderToken;
    }

    protected String getVisaProviderToken() {
        if (visaProviderToken == null) {
            visaProviderToken = getTokenForUser("visa1@travelify.com");
        }
        return visaProviderToken;
    }

    protected String getCustomerToken() {
        if (customerToken == null) {
            customerToken = getTokenForUser("customer1@travelify.com");
        }
        return customerToken;
    }

    protected String toJson(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }
}
