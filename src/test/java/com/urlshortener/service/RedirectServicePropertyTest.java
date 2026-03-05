package com.urlshortener.service;

import com.urlshortener.dto.RedirectResponse;
import com.urlshortener.dto.ShortenedUrlDto;
import com.urlshortener.exception.ShortUrlNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Property-based tests for RedirectService using JUnit's @RepeatedTest.
 * These tests verify universal properties across many randomized inputs.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RedirectServicePropertyTest {
    
    @Autowired
    private RedirectService redirectService;
    
    @Autowired
    private URLService urlService;
    
    private final SecureRandom random = new SecureRandom();
    
    @BeforeEach
    void setUp() {
        // Database is cleaned between tests due to @Transactional
    }
    
    /**
     * Feature: acortador-urls, Property 7: Non-existent Short Code Handling
     * **Validates: Requirements 3.2, 7.3**
     * 
     * For any short code that does not exist in the database, requesting a redirect 
     * should return HTTP 404 with an error message.
     */
    @RepeatedTest(10)
    void nonExistentShortCodeShouldThrowNotFoundException() {
        // Generate a random short code that doesn't exist
        String nonExistentCode = generateRandomShortCode();
        
        // Verify that attempting to redirect throws ShortUrlNotFoundException
        assertThatThrownBy(() -> redirectService.handleRedirect(nonExistentCode))
            .as("Non-existent short code should throw ShortUrlNotFoundException")
            .isInstanceOf(ShortUrlNotFoundException.class)
            .hasMessageContaining(nonExistentCode);
    }
    
    /**
     * Feature: acortador-urls, Property 8: Stored URL Validity
     * **Validates: Requirements 3.3**
     * 
     * For any URL mapping stored in the database, the long URL should remain in a valid format
     * (valid protocol and domain).
     */
    @RepeatedTest(10)
    void storedUrlShouldBeValidBeforeRedirecting() {
        // Generate and store a valid URL
        String validUrl = generateRandomValidUrl();
        ShortenedUrlDto shortened = urlService.createShortUrl(validUrl);
        String shortCode = shortened.getShortCode();
        
        // Attempt to redirect - should succeed without throwing InvalidUrlException
        RedirectResponse response = redirectService.handleRedirect(shortCode);
        
        // Verify the response
        assertThat(response)
            .as("Redirect response should not be null")
            .isNotNull();
        
        assertThat(response.getTargetUrl())
            .as("Target URL should match the original valid URL")
            .isEqualTo(validUrl);
        
        assertThat(response.getStatusCode())
            .as("Status code should be 301 (Moved Permanently)")
            .isEqualTo(301);
    }
    
    /**
     * Generates a random short code that is unlikely to exist in the database.
     * Format: 7 alphanumeric characters
     */
    private String generateRandomShortCode() {
        String alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder code = new StringBuilder(7);
        for (int i = 0; i < 7; i++) {
            code.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return code.toString();
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
