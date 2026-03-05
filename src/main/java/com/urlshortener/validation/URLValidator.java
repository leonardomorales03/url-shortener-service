package com.urlshortener.validation;

/**
 * Interface for validating URL format and structure
 */
public interface URLValidator {
    /**
     * Validates a URL format and structure
     * @param url the URL to validate
     * @return ValidationResult containing validation status and errors
     */
    ValidationResult validate(String url);
}
