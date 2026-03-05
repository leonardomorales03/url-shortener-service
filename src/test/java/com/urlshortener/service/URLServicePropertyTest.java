package com.urlshortener.service;

import com.urlshortener.dto.ShortenedUrlDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for URLService using JUnit's @RepeatedTest.
 * These tests verify universal properties across many randomized inputs.
 * 
 * Note: Using @RepeatedTest instead of jqwik @Property because jqwik doesn't
 * integrate well with Spring Boot's dependency injection.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class URLServicePropertyTest {
    
    @Autowired
    private URLService urlService;
    
    private final SecureRandom random = new SecureRandom();
    
    @BeforeEach
    void setUp() {
        // Database is cleaned between tests due to @Transactional
    }
    
    /**
     * Feature: acortador-urls, Property 2: URL Shortening Round Trip
     * **Validates: Requirements 1.2, 3.1**
     * 
     * For any valid long URL, shortening it to get a short code and then retrieving 
     * the long URL using that short code should return the original URL.
     */
    @RepeatedTest(10)
    void shortenedUrlShouldRetrieveOriginalUrl() {
        // Generate a random valid URL
        String url = generateRandomValidUrl();
        
        // Shorten the URL
        ShortenedUrlDto shortened = urlService.createShortUrl(url);
        String shortCode = shortened.getShortCode();
        
        // Retrieve the original URL using the short code
        Optional<String> retrieved = urlService.getLongUrl(shortCode);
        
        // Verify the round trip
        assertThat(retrieved)
            .as("Short code should retrieve a URL")
            .isPresent();
        
        assertThat(retrieved.get())
            .as("Retrieved URL should match the original URL")
            .isEqualTo(url);
    }
    
    /**
     * Feature: acortador-urls, Property 3: Idempotent URL Shortening
     * **Validates: Requirements 1.3**
     * 
     * For any valid long URL, shortening it multiple times should always return the same short code.
     */
    @RepeatedTest(10)
    void shorteningSameUrlMultipleTimesShouldReturnSameCode() {
        // Generate a random valid URL
        String url = generateRandomValidUrl();
        
        // Shorten the URL twice
        ShortenedUrlDto first = urlService.createShortUrl(url);
        ShortenedUrlDto second = urlService.createShortUrl(url);
        
        // Verify idempotency
        assertThat(first.getShortCode())
            .as("Same URL should always return the same short code (idempotent)")
            .isEqualTo(second.getShortCode());
        
        assertThat(first.getOriginalUrl())
            .as("Original URL should be preserved")
            .isEqualTo(url);
        
        assertThat(second.getOriginalUrl())
            .as("Original URL should be preserved")
            .isEqualTo(url);
    }
    
    /**
     * Generates a random valid URL for testing.
     * Format: (http|https)://[domain].com/[path]
     */
    private String generateRandomValidUrl() {
        String protocol = random.nextBoolean() ? "http://" : "https://";
        String domain = generateRandomString(3, 15, "abcdefghijklmnopqrstuvwxyz") + ".com";
        String path = random.nextBoolean() ? "" : "/" + generateRandomString(0, 50, "abcdefghijklmnopqrstuvwxyz0123456789/-_");
        return protocol + domain + path;
    }
    
    /**
     * Generates a random string with characters from the given alphabet.
     */
    private String generateRandomString(int minLength, int maxLength, String alphabet) {
        int length = minLength + random.nextInt(maxLength - minLength + 1);
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return sb.toString();
    }
}
