-- Test data for Travelify integration tests

-- Insert test users
INSERT INTO users (id, email, password, first_name, last_name, phone, role, provider_type, business_name, business_address, avatar_url, is_active, created_at, updated_at, last_login_at)
VALUES 
    (1, 'admin@travelify.com', 'password123', 'Admin', 'User', '+1234567890', 'ADMIN', NULL, 'Travelify Admin', '123 Admin St', NULL, true, NOW(), NOW(), NULL),
    (2, 'agent1@travelify.com', 'password123', 'Travel', 'Agent', '+1234567891', 'AGENT', 'TRAVEL_AGENT', 'Agent Travel Co', '456 Agent Ave', NULL, true, NOW(), NOW(), NULL),
    (3, 'hotel1@travelify.com', 'password123', 'Hotel', 'Manager', '+1234567892', 'AGENT', 'HOTEL', 'Grand Hotel', '789 Hotel Blvd', NULL, true, NOW(), NOW(), NULL),
    (4, 'insurance1@travelify.com', 'password123', 'Insurance', 'Broker', '+1234567893', 'AGENT', 'INSURANCE', 'Safe Travel Insurance', '101 Insurance St', NULL, true, NOW(), NOW(), NULL),
    (5, 'visa1@travelify.com', 'password123', 'Visa', 'Specialist', '+1234567894', 'AGENT', 'VISA', 'Quick Visa Services', '202 Visa Ave', NULL, true, NOW(), NOW(), NULL),
    (6, 'customer1@travelify.com', 'password123', 'John', 'Doe', '+1234567895', 'CUSTOMER', NULL, NULL, NULL, NULL, true, NOW(), NOW(), NULL);

-- Insert trip categories
INSERT INTO trip_categories (id, name, slug, description, icon, sort_order, is_active, created_at, updated_at)
VALUES 
    (1, 'Adventure', 'adventure', 'Adventure trips and expeditions', 'icon-adventure', 1, true, NOW(), NOW()),
    (2, 'Beach', 'beach', 'Beach and relaxation vacations', 'icon-beach', 2, true, NOW(), NOW()),
    (3, 'Cultural', 'cultural', 'Cultural and historical tours', 'icon-cultural', 3, true, NOW(), NOW()),
    (4, 'Mountain', 'mountain', 'Mountain climbing and hiking', 'icon-mountain', 4, true, NOW(), NOW()),
    (5, 'Wildlife', 'wildlife', 'Wildlife safaris and nature tours', 'icon-wildlife', 5, true, NOW(), NOW());

-- Insert trip templates (created by admin)
INSERT INTO trip_templates (id, title, slug, short_description, full_description, featured_image, category_id, difficulty, duration_days, min_age, max_group_size, is_featured, status, created_by, created_at, updated_at)
VALUES 
    (1, 'Bali Adventure', 'bali-adventure', '7-day Bali adventure tour', 'Full description of Bali adventure', 'https://picsum.photos/800/600?random=1', 1, 'MODERATE', 7, 12, 20, true, 'PUBLISHED', 1, NOW(), NOW()),
    (2, 'Maldives Beach', 'maldives-beach', '5-day Maldives beach vacation', 'Full description of Maldives beach', 'https://picsum.photos/800/600?random=2', 2, 'EASY', 5, 5, 15, false, 'PUBLISHED', 1, NOW(), NOW()),
    (3, 'Rome Cultural', 'rome-cultural', '4-day Rome cultural tour', 'Full description of Rome cultural', 'https://picsum.photos/800/600?random=3', 3, 'EASY', 4, 8, 25, false, 'DRAFT', 1, NOW(), NOW()),
    (4, 'Everest Base Camp', 'everest-base-camp', '14-day Everest trek', 'Full description of Everest trek', 'https://picsum.photos/800/600?random=4', 4, 'HARD', 14, 18, 10, true, 'PUBLISHED', 1, NOW(), NOW()),
    (5, 'African Safari', 'african-safari', '10-day African wildlife safari', 'Full description of African safari', 'https://picsum.photos/800/600?random=5', 5, 'MODERATE', 10, 12, 12, false, 'PUBLISHED', 1, NOW(), NOW());

-- Insert services (created by providers)
INSERT INTO services (id, provider_id, service_type, name, description, price, currency, meta, status, created_at, updated_at)
VALUES 
    (1, 3, 'HOTEL_ROOM', 'Deluxe Ocean View', 'Luxurious room with ocean view', 250.00, 'USD', '{"roomType": "Deluxe"}', 'PUBLISHED', NOW(), NOW()),
    (2, 3, 'HOTEL_ROOM', 'Standard Room', 'Comfortable standard room', 150.00, 'USD', '{"roomType": "Standard"}', 'PUBLISHED', NOW(), NOW()),
    (3, 3, 'HOTEL_ROOM', 'Family Suite', 'Spacious suite for families', 400.00, 'USD', '{"roomType": "Suite"}', 'PUBLISHED', NOW(), NOW()),
    (4, 3, 'HOTEL_ROOM', 'Penthouse Suite', 'Luxury penthouse with city view', 800.00, 'USD', '{"roomType": "Penthouse"}', 'DRAFT', NOW(), NOW()),
    (5, 3, 'HOTEL_ROOM', 'Poolside Room', 'Room with direct pool access', 200.00, 'USD', '{"roomType": "Poolside"}', 'PUBLISHED', NOW(), NOW()),
    (6, 4, 'INSURANCE_PLAN', 'Basic Travel Insurance', 'Basic coverage for travel', 50.00, 'USD', '{"coverageType": "Basic"}', 'PUBLISHED', NOW(), NOW()),
    (7, 4, 'INSURANCE_PLAN', 'Premium Travel Insurance', 'Comprehensive travel coverage', 150.00, 'USD', '{"coverageType": "Premium"}', 'PUBLISHED', NOW(), NOW()),
    (8, 4, 'INSURANCE_PLAN', 'Family Travel Insurance', 'Coverage for entire family', 200.00, 'USD', '{"coverageType": "Family"}', 'PUBLISHED', NOW(), NOW()),
    (9, 4, 'INSURANCE_PLAN', 'Adventure Sports Insurance', 'Special coverage for adventure activities', 250.00, 'USD', '{"coverageType": "Adventure"}', 'DRAFT', NOW(), NOW()),
    (10, 5, 'VISA_SERVICE', 'US Tourist Visa', 'Tourist visa for USA', 160.00, 'USD', '{"country": "USA"}', 'PUBLISHED', NOW(), NOW()),
    (11, 5, 'VISA_SERVICE', 'UK Standard Visa', 'Standard visitor visa for UK', 120.00, 'USD', '{"country": "UK"}', 'PUBLISHED', NOW(), NOW()),
    (12, 5, 'VISA_SERVICE', 'Schengen Visa', 'Schengen area visa', 80.00, 'USD', '{"country": "Schengen"}', 'PUBLISHED', NOW(), NOW()),
    (13, 5, 'VISA_SERVICE', 'Australia ETA', 'Electronic Travel Authority', 20.00, 'USD', '{"country": "Australia"}', 'PUBLISHED', NOW(), NOW()),
    (14, 5, 'VISA_SERVICE', 'Canada Visitor Visa', 'Visitor visa for Canada', 100.00, 'USD', '{"country": "Canada"}', 'PUBLISHED', NOW(), NOW()),
    (15, 5, 'VISA_SERVICE', 'Japan Tourist Visa', 'Tourist visa for Japan', 40.00, 'USD', '{"country": "Japan"}', 'DRAFT', NOW(), NOW());

-- Insert agent trips (created by travel agent from templates)
INSERT INTO agent_trips (id, template_id, agent_id, title, slug, short_description, full_description, featured_image, category_id, difficulty, duration_days, min_age, max_group_size, is_featured, status, override_fields, created_at, updated_at)
VALUES 
    (1, 1, 2, 'Bali Adventure - Agent Version', 'bali-adventure-agent-1', '7-day Bali adventure by Agent Travel Co', 'Full description by agent', 'https://picsum.photos/800/600?random=10', 1, 'MODERATE', 7, 12, 20, false, 'PUBLISHED', '{"title": true}', NOW(), NOW()),
    (2, 1, 2, 'Bali Adventure - Summer Special', 'bali-adventure-summer', '7-day Bali summer special', 'Summer special description', 'https://picsum.photos/800/600?random=11', 1, 'MODERATE', 7, 12, 20, true, 'PUBLISHED', '{"title": true}', NOW(), NOW()),
    (3, 2, 2, 'Maldives Beach Getaway', 'maldives-beach-agent-1', '5-day beach vacation in Maldives', 'Agent beach vacation description', 'https://picsum.photos/800/600?random=12', 2, 'EASY', 5, 5, 15, false, 'PUBLISHED', '{"title": true}', NOW(), NOW()),
    (4, NULL, 2, 'Custom Trip to Thailand', 'custom-thailand', 'Custom Thailand trip created from scratch', 'Full custom description', 'https://picsum.photos/800/600?random=13', 1, 'MODERATE', 10, 12, 25, false, 'DRAFT', '{"title": true}', NOW(), NOW());

-- Insert trip services (linking agent trips to services)
INSERT INTO trip_services (id, agent_trip_id, service_id, is_optional, override_price, created_at)
VALUES 
    (1, 1, 1, true, 240.00, NOW()),
    (2, 1, 6, true, NULL, NOW());
