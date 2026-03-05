package com.urlshortener.exception;

/**
 * Exception thrown when a short code does not exist in the database.
 * Used by RedirectService when attempting to redirect to a non-existent short code.
 */
public class ShortUrlNotFoundException extends RuntimeException {
    public ShortUrlNotFoundException(String message) {
        super(message);
    }
    
    public ShortUrlNotFoundException(String shortCode, String message) {
        super(String.format("Short code '%s' not found: %s", shortCode, message));
    }
}
