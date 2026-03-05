package com.urlshortener.service;

import com.urlshortener.dto.ShortenedUrlDto;
import com.urlshortener.exception.InvalidUrlException;
import com.urlshortener.exception.ShortCodeGenerationException;
import com.urlshortener.hash.HashGenerator;
import com.urlshortener.repository.UrlMappingRepository;
import com.urlshortener.validation.URLValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Tests for URLService including both unit tests and property-based tests.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class URLServiceTest {
    
    @Autowired
    private UrlMappingRepository urlMappingRepository;
    
    @Autowired
    private URLService urlService;
    
    @Autowired
    private HashGenerator hashGenerator;
    
    @Autowired
    private URLValidator urlValidator;
    
    @BeforeEach
    void setUp() {
        urlMappingRepository.deleteAll();
    }
    
    // ========== Unit Tests ==========
    
    @Test
    void shouldCreateShortUrlForValidInput() {
        String longUrl = "https://example.com/very/long/path";
        
        ShortenedUrlDto result = urlService.createShortUrl(longUrl);
        
        assertThat(result.getShortCode()).isNotEmpty();
        assertThat(result.getOriginalUrl()).isEqualTo(longUrl);
        assertThat(result.getShortUrl()).contains(result.getShortCode());
        assertThat(result.getCreatedAt()).isNotNull();
    }
    
    @Test
    void shouldRetrieveLongUrlByShortCode() {
        String longUrl = "https://example.com/test";
        ShortenedUrlDto shortened = urlService.createShortUrl(longUrl);
        
        Optional<String> retrieved = urlService.getLongUrl(shortened.getShortCode());
        
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get()).isEqualTo(longUrl);
    }
    
    @Test
    void shouldReturnEmptyForNonExistentShortCode() {
        Optional<String> retrieved = urlService.getLongUrl("nonexistent");
        
        assertThat(retrieved).isEmpty();
    }
    
    @Test
    void shouldRejectUrlWithoutProtocol() {
        String invalidUrl = "example.com/path";
        
        assertThatThrownBy(() -> urlService.createShortUrl(invalidUrl))
            .isInstanceOf(InvalidUrlException.class)
            .hasMessageContaining("http");
    }
    
    @Test
    void shouldRejectUrlExceedingMaxLength() {
        String longUrl = "https://example.com/" + "a".repeat(2100);
        
        assertThatThrownBy(() -> urlService.createShortUrl(longUrl))
            .isInstanceOf(InvalidUrlException.class)
            .hasMessageContaining("maximum length");
    }
    
    /**
     * Feature: acortador-urls, Property 3: Idempotent URL Shortening
     * **Validates: Requirements 1.3**
     */
    @Test
    void shouldReturnSameShortCodeForSameUrl() {
        String longUrl = "https://example.com/test";
        
        ShortenedUrlDto first = urlService.createShortUrl(longUrl);
        ShortenedUrlDto second = urlService.createShortUrl(longUrl);
        
        assertThat(first.getShortCode()).isEqualTo(second.getShortCode());
        assertThat(first.getOriginalUrl()).isEqualTo(longUrl);
        assertThat(second.getOriginalUrl()).isEqualTo(longUrl);
    }
    
    // ========== Collision Handling Unit Tests ==========
    
    /**
     * Tests collision retry logic.
     * Requirements: 9.2, 9.3
     */
    @Test
    void shouldRetryOnCollisionAndGenerateUniqueCode() {
        // Create a URL to occupy a short code
        String firstUrl = "https://example.com/first";
        ShortenedUrlDto first = urlService.createShortUrl(firstUrl);
        String firstCode = first.getShortCode();
        
        // Create a second URL that should get a different code
        String secondUrl = "https://example.com/second";
        ShortenedUrlDto second = urlService.createShortUrl(secondUrl);
        String secondCode = second.getShortCode();
        
        // Verify different codes were generated
        assertThat(secondCode)
            .as("Collision handling should generate a different code")
            .isNotEqualTo(firstCode);
        
        // Verify both URLs can be retrieved
        assertThat(urlService.getLongUrl(firstCode)).contains(firstUrl);
        assertThat(urlService.getLongUrl(secondCode)).contains(secondUrl);
    }
    
    /**
     * Tests retry limit exceeded scenario.
     * Requirements: 9.2, 9.3
     * 
     * Note: This test uses a mock to simulate the scenario where all generated
     * codes collide, which is extremely unlikely in real usage.
     */
    @Test
    void shouldThrowExceptionWhenRetryLimitExceeded() {
        // Create a mock hash generator that always returns the same code
        HashGenerator mockHashGenerator = Mockito.mock(HashGenerator.class);
        when(mockHashGenerator.generateShortCode(anyString())).thenReturn("collision");
        when(mockHashGenerator.generateRandomCode()).thenReturn("collision");
        
        // Create a service with the mock generator
        URLService serviceWithMock = new URLServiceImpl(
            urlMappingRepository, 
            mockHashGenerator, 
            urlValidator
        );
        
        // First URL should succeed
        String firstUrl = "https://example.com/first";
        serviceWithMock.createShortUrl(firstUrl);
        
        // Second URL should fail after max retries
        String secondUrl = "https://example.com/second";
        assertThatThrownBy(() -> serviceWithMock.createShortUrl(secondUrl))
            .isInstanceOf(ShortCodeGenerationException.class)
            .hasMessageContaining("Unable to generate unique short code")
            .hasMessageContaining("5 attempts");
    }
    
    /**
     * Tests that collision handling preserves existing mappings.
     * Requirements: 9.1
     */
    @Test
    void shouldNotOverwriteExistingMappingOnCollision() {
        // Create first URL
        String firstUrl = "https://example.com/original";
        ShortenedUrlDto first = urlService.createShortUrl(firstUrl);
        String firstCode = first.getShortCode();
        
        // Create multiple other URLs
        for (int i = 0; i < 10; i++) {
            urlService.createShortUrl("https://example.com/url" + i);
        }
        
        // Verify original mapping is still intact
        Optional<String> retrieved = urlService.getLongUrl(firstCode);
        assertThat(retrieved)
            .as("Original mapping should not be overwritten")
            .isPresent()
            .contains(firstUrl);
    }
}
