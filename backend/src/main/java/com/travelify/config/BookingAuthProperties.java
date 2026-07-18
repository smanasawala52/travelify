package com.travelify.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * WP Travel–style booking identity options:
 * - allow guests to book without logging in
 * - optionally create a customer account automatically during checkout
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "travelify.booking")
public class BookingAuthProperties {

    /** When true, customers may start a booking without being logged in. */
    private boolean allowGuestBooking = true;

    /** When true, create a CUSTOMER account automatically during guest checkout. */
    private boolean createAccountAutomatically = true;
}
