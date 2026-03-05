package com.urlshortener.hash;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Base62HashGenerator.
 * Tests specific examples and edge cases.
 */
class Base62HashGeneratorTest {
    
    private Base62HashGenerator hashGenerator;
    
    @BeforeEach
    void setUp() {
        hashGenerator = new Base62HashGenerator();
    }
    
    @Test
    void shouldGenerateShortCodeWithCorrectLength() {
        String url = "https://example.com/test";
        String shortCode = hashGenerator.generateShortCode(url);
        
        assertThat(shortCode).hasSize(7);
    }
    
    @Test
    void shouldGenerateShortCodeWithOnlyAlphanumericCharacters() {
        String url = "https://example.com/test";
        String shortCode = hashGenerator.generateShortCode(url);
        
        assertThat(shortCode).matches("[a-zA-Z0-9]+");
    }
    
    @Test
    void shouldGenerateDeterministicShortCode() {
        String url = "https://example.com/test";
        String firstCode = hashGenerator.generateShortCode(url);
        String secondCode = hashGenerator.generateShortCode(url);
        
        assertThat(firstCode).isEqualTo(secondCode);
    }
    
    @Test
    void shouldGenerateDifferentCodesForDifferentUrls() {
        String url1 = "https://example.com/test1";
        String url2 = "https://example.com/test2";
        
        String code1 = hashGenerator.generateShortCode(url1);
        String code2 = hashGenerator.generateShortCode(url2);
        
        assertThat(code1).isNotEqualTo(code2);
    }
    
    @Test
    void shouldGenerateRandomCodeWithCorrectLength() {
        String randomCode = hashGenerator.generateRandomCode();
        
        assertThat(randomCode).hasSize(7);
    }
    
    @Test
    void shouldGenerateRandomCodeWithOnlyAlphanumericCharacters() {
        String randomCode = hashGenerator.generateRandomCode();
        
        assertThat(randomCode).matches("[a-zA-Z0-9]+");
    }
    
    @Test
    void shouldGenerateDifferentRandomCodes() {
        String code1 = hashGenerator.generateRandomCode();
        String code2 = hashGenerator.generateRandomCode();
        
        // Statistically, these should be different
        assertThat(code1).isNotEqualTo(code2);
    }
    
    @Test
    void shouldHandleVeryLongUrls() {
        String longUrl = "https://example.com/" + "a".repeat(2000);
        String shortCode = hashGenerator.generateShortCode(longUrl);
        
        assertThat(shortCode).hasSize(7);
        assertThat(shortCode).matches("[a-zA-Z0-9]+");
    }
    
    @Test
    void shouldHandleUrlsWithSpecialCharacters() {
        String url = "https://example.com/path?query=value&foo=bar#fragment";
        String shortCode = hashGenerator.generateShortCode(url);
        
        assertThat(shortCode).hasSize(7);
        assertThat(shortCode).matches("[a-zA-Z0-9]+");
    }
}
