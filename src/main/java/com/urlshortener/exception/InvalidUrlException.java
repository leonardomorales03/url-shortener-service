package com.urlshortener.exception;

/**
 * Exception thrown when a URL fails validation.
 * 
 * Requirements:
 * - 1.4: Reject invalid URL format with descriptive error
 * - 4.3: Reject URLs with invalid characters or malformed syntax
 */
public class InvalidUrlException extends RuntimeException {
    
    public InvalidUrlException(String message) {
        super(message);
    }
    
    public InvalidUrlException(String message, Throwable cause) {
        super(message, cause);
    }
}
