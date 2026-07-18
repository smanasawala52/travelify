package com.travelify.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.function.Predicate;

/**
 * URL-safe slug generation with uniqueness helpers.
 */
public final class SlugUtils {

    private SlugUtils() {}

    public static String slugify(String input) {
        if (input == null || input.isBlank()) {
            return "item";
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        String slug = normalized.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        return slug.isBlank() ? "item" : slug;
    }

    /**
     * Returns {@code base} or {@code base-2}, {@code base-3}, ... until {@code exists} is false.
     */
    public static String uniqueSlug(String base, Predicate<String> exists) {
        String candidate = base;
        int n = 2;
        while (exists.test(candidate)) {
            candidate = base + "-" + n;
            n++;
            if (n > 10_000) {
                throw new IllegalStateException("Unable to generate unique slug from: " + base);
            }
        }
        return candidate;
    }
}
