package com.urlshortener.service;

import com.urlshortener.dto.ShortenedUrlDto;

import java.util.Optional;

/**
 * Service interface for URL shortening operations.
 * 
 * Requirements:
 * - 1.1: Generate unique short code for valid long URL
 * - 1.2: Store mapping between short code and long URL
 * - 1.3: Return existing short code for duplicate URLs
 */
public interface URLService {
    /**
     * Creates a short URL for the given long URL.
     * If the URL already exists, returns the existing short code.
     * 
     * @param longUrl the original URL to shorten
     * @return ShortenedUrlDto containing the short code and metadata
     * @throws com.urlshortener.exception.InvalidUrlException if the URL is invalid
     * @throws com.urlshortener.exception.ShortCodeGenerationException if unable to generate unique code
     */
    ShortenedUrlDto createShortUrl(String longUrl);
    
    /**
     * Retrieves the original URL for a given short code.
     * 
     * @param shortCode the short code to lookup
     * @return Optional containing the long URL if found
     */
    Optional<String> getLongUrl(String shortCode);
}
