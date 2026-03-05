package com.urlshortener.validation;

import net.jqwik.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for URL validation
 */
class URLValidatorPropertyTest {

    private final URLValidator urlValidator = new URLValidatorImpl();

    // Feature: acortador-urls, Property 4: Invalid URL Rejection
    // **Validates: Requirements 1.4, 4.1, 4.2, 4.3**
    @Property(tries = 10)
    void invalidUrlsShouldBeRejected(@ForAll("invalidUrls") String url) {
        ValidationResult result = urlValidator.validate(url);
        
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).isNotEmpty();
    }

    @Provide
    Arbitrary<String> invalidUrls() {
        return Arbitraries.oneOf(
            // Missing protocol (Requirement 4.1)
            Arbitraries.strings()
                .alpha().numeric()
                .ofMinLength(5)
                .ofMaxLength(50)
                .map(s -> "www." + s + ".com"),
            
            // Invalid protocol (Requirement 4.1)
            Arbitraries.strings()
                .alpha().numeric()
                .ofMinLength(5)
                .ofMaxLength(50)
                .map(s -> "ftp://" + s + ".com"),
            
            // No domain (Requirement 4.2)
            Arbitraries.just("http://"),
            Arbitraries.just("https://"),
            
            // Invalid domain format - no TLD (Requirement 4.2)
            Arbitraries.strings()
                .alpha().numeric()
                .ofMinLength(1)
                .ofMaxLength(20)
                .map(s -> "http://" + s),
            
            // Invalid characters - spaces (Requirement 4.3)
            Arbitraries.strings()
                .alpha().numeric()
                .ofMinLength(1)
                .ofMaxLength(20)
                .map(s -> "http://example.com/path with spaces/" + s),
            
            // Invalid characters - control characters (Requirement 4.3)
            Arbitraries.strings()
                .withChars('\u0000', '\u001F')
                .ofMinLength(1)
                .ofMaxLength(10)
                .map(s -> "http://example.com/" + s),
            
            // Malformed syntax (Requirement 4.2)
            Arbitraries.just("not a url at all"),
            Arbitraries.just("http://.com"),
            Arbitraries.just("http://-.com")
        );
    }
}
