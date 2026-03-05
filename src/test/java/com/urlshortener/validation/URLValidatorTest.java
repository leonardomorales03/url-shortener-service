package com.urlshortener.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for URL validation edge cases
 * Requirements: 4.4, 4.5
 */
class URLValidatorTest {

    private URLValidator urlValidator;

    @BeforeEach
    void setUp() {
        urlValidator = new URLValidatorImpl();
    }

    // Requirement 4.4: Test maximum length boundary (2048 characters)
    @Test
    void shouldAcceptUrlAtMaximumLength() {
        // Create a URL exactly 2048 characters long
        String baseUrl = "https://example.com/";
        int remainingLength = 2048 - baseUrl.length();
        String longPath = "a".repeat(remainingLength);
        String url = baseUrl + longPath;

        ValidationResult result = urlValidator.validate(url);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
    }

    // Requirement 4.5: Test URLs exceeding maximum length
    @Test
    void shouldRejectUrlExceedingMaximumLength() {
        // Create a URL with 2049 characters (1 over the limit)
        String baseUrl = "https://example.com/";
        int excessLength = 2049 - baseUrl.length();
        String longPath = "a".repeat(excessLength);
        String url = baseUrl + longPath;

        ValidationResult result = urlValidator.validate(url);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0)).contains("maximum length");
    }

    // Requirement 4.5: Test URLs significantly exceeding maximum length
    @Test
    void shouldRejectUrlSignificantlyExceedingMaximumLength() {
        String url = "https://example.com/" + "a".repeat(3000);

        ValidationResult result = urlValidator.validate(url);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).contains("URL exceeds maximum length of 2048 characters");
    }

    // Requirement 4.4: Test various invalid formats - empty URL
    @Test
    void shouldRejectEmptyUrl() {
        ValidationResult result = urlValidator.validate("");

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).contains("URL cannot be empty");
    }

    // Requirement 4.4: Test various invalid formats - null URL
    @Test
    void shouldRejectNullUrl() {
        ValidationResult result = urlValidator.validate(null);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).contains("URL cannot be empty");
    }

    // Requirement 4.4: Test various invalid formats - whitespace only
    @Test
    void shouldRejectWhitespaceOnlyUrl() {
        ValidationResult result = urlValidator.validate("   ");

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).contains("URL cannot be empty");
    }

    // Requirement 4.4: Test various invalid formats - missing protocol
    @Test
    void shouldRejectUrlWithoutProtocol() {
        ValidationResult result = urlValidator.validate("example.com/path");

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(error -> error.contains("http://") || error.contains("https://"));
    }

    // Requirement 4.4: Test various invalid formats - invalid protocol
    @Test
    void shouldRejectUrlWithInvalidProtocol() {
        ValidationResult result = urlValidator.validate("ftp://example.com");

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(error -> error.contains("http://") || error.contains("https://"));
    }

    // Requirement 4.4: Test various invalid formats - missing domain
    @Test
    void shouldRejectUrlWithoutDomain() {
        ValidationResult result = urlValidator.validate("http://");

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(error -> error.contains("domain"));
    }

    // Requirement 4.4: Test various invalid formats - invalid domain (no TLD)
    @Test
    void shouldRejectUrlWithoutTld() {
        ValidationResult result = urlValidator.validate("http://example");

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(error -> error.contains("domain"));
    }

    // Requirement 4.4: Test various invalid formats - domain starting with hyphen
    @Test
    void shouldRejectDomainStartingWithHyphen() {
        ValidationResult result = urlValidator.validate("http://-example.com");

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(error -> error.contains("domain"));
    }

    // Requirement 4.4: Test various invalid formats - domain ending with hyphen
    @Test
    void shouldRejectDomainEndingWithHyphen() {
        ValidationResult result = urlValidator.validate("http://example-.com");

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(error -> error.contains("domain"));
    }

    // Requirement 4.4: Test various invalid formats - URL with spaces
    @Test
    void shouldRejectUrlWithSpaces() {
        ValidationResult result = urlValidator.validate("http://example.com/path with spaces");

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(error -> error.contains("invalid characters"));
    }

    // Requirement 4.4: Test various invalid formats - URL with control characters
    @Test
    void shouldRejectUrlWithControlCharacters() {
        ValidationResult result = urlValidator.validate("http://example.com/path\u0000test");

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(error -> error.contains("invalid characters"));
    }

    // Test valid URLs to ensure validator doesn't reject good URLs
    @Test
    void shouldAcceptValidHttpUrl() {
        ValidationResult result = urlValidator.validate("http://example.com");

        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    void shouldAcceptValidHttpsUrl() {
        ValidationResult result = urlValidator.validate("https://example.com");

        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    void shouldAcceptUrlWithPath() {
        ValidationResult result = urlValidator.validate("https://example.com/path/to/resource");

        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    void shouldAcceptUrlWithQueryParameters() {
        ValidationResult result = urlValidator.validate("https://example.com/path?param=value&other=123");

        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    void shouldAcceptUrlWithSubdomain() {
        ValidationResult result = urlValidator.validate("https://subdomain.example.com");

        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    void shouldAcceptUrlWithMultipleSubdomains() {
        ValidationResult result = urlValidator.validate("https://sub1.sub2.example.com");

        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    void shouldAcceptUrlWithHyphenInDomain() {
        ValidationResult result = urlValidator.validate("https://my-example.com");

        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    void shouldAcceptUrlWithNumbers() {
        ValidationResult result = urlValidator.validate("https://example123.com");

        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
    }
}
