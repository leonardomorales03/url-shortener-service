package com.urlshortener.hash;

/**
 * Interface for generating short codes from URLs.
 * Implementations should provide both hash-based and random code generation.
 */
public interface HashGenerator {
    /**
     * Generates a short code from a long URL using a hash algorithm.
     * The generated code should be deterministic for the same input URL.
     * 
     * @param longUrl the URL to hash
     * @return a short alphanumeric code (6-10 characters)
     */
    String generateShortCode(String longUrl);
    
    /**
     * Generates a random short code (used for collision resolution).
     * Each call should produce a different random code.
     * 
     * @return a random alphanumeric code (6-10 characters)
     */
    String generateRandomCode();
}
