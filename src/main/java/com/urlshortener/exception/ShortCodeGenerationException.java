package com.urlshortener.exception;

/**
 * Exception thrown when unable to generate a unique short code.
 * 
 * Requirements:
 * - 9.3: Return error when collision retry limit is exceeded
 */
public class ShortCodeGenerationException extends RuntimeException {
    
    public ShortCodeGenerationException(String message) {
        super(message);
    }
    
    public ShortCodeGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
