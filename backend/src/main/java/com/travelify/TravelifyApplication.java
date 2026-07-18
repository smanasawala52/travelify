package com.travelify;

import com.travelify.config.BookingAuthProperties;
import com.travelify.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({JwtProperties.class, BookingAuthProperties.class})
public class TravelifyApplication {
    public static void main(String[] args) {
        SpringApplication.run(TravelifyApplication.class, args);
    }
}
