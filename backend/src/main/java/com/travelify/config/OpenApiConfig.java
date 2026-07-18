package com.travelify.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI travelifyOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Travelify API")
                        .description("""
                                Travel booking platform API.
                                JWT auth is a modern alternative to WP Travel application passwords.
                                Role gates map to WP Travel / WordPress user roles (CUSTOMER, AGENT, ADMIN).
                                """)
                        .version("1.0.0")
                        .contact(new Contact().name("Travelify").email("admin@travelify.com"))
                        .license(new License().name("MIT")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .name("bearerAuth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Paste access token from /api/auth/login")));
    }
}
