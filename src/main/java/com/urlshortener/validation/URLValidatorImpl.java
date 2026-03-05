package com.urlshortener.validation;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Implementation of URLValidator that validates URL format and structure
 * according to requirements 4.1-4.5
 */
@Component
public class URLValidatorImpl implements URLValidator {
    private static final int MAX_URL_LENGTH = 2048;
    // More strict pattern: domain parts must start with alphanumeric, can contain hyphens in middle
    private static final Pattern URL_PATTERN = Pattern.compile(
        "^(https?://)([a-zA-Z0-9]([a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,}(/.*)?$"
    );
    // Pattern to detect invalid characters (control characters and spaces)
    private static final Pattern INVALID_CHARS_PATTERN = Pattern.compile("[\\x00-\\x1F\\x7F\\s]");

    @Override
    public ValidationResult validate(String url) {
        List<String> errors = new ArrayList<>();

        // Check for null or empty
        if (url == null || url.trim().isEmpty()) {
            errors.add("URL cannot be empty");
            return ValidationResult.invalid(errors);
        }

        // Requirement 4.5: Check maximum length (2048 characters)
        if (url.length() > MAX_URL_LENGTH) {
            errors.add("URL exceeds maximum length of " + MAX_URL_LENGTH + " characters");
        }

        // Requirement 4.1: Check protocol (http or https)
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            errors.add("URL must start with http:// or https://");
        }

        // Requirement 4.3: Check for invalid characters
        if (INVALID_CHARS_PATTERN.matcher(url).find()) {
            errors.add("URL contains invalid characters (control characters or spaces)");
        }

        // Requirement 4.2: Check domain format and overall URL structure
        if (!URL_PATTERN.matcher(url).matches()) {
            errors.add("URL format is invalid - must contain a valid domain name");
        }

        return errors.isEmpty() ? 
            ValidationResult.valid() : 
            ValidationResult.invalid(errors);
    }
}
