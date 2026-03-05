package com.urlshortener.hash;

import net.jqwik.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for HashGenerator implementations.
 * Tests universal properties that should hold for all valid inputs.
 */
class HashGeneratorPropertyTest {
    
    private final HashGenerator hashGenerator = new Base62HashGenerator();
    
    /**
     * Feature: acortador-urls, Property 1: Short Code Generation Invariants
     * **Validates: Requirements 1.1, 1.5, 1.6**
     * 
     * For any valid long URL, when a short code is generated, the code should be 
     * between 6 and 10 characters in length and contain only alphanumeric characters (a-z, A-Z, 0-9).
     */
    @Property(tries = 10)
    void generatedShortCodesShouldMeetFormatRequirements(@ForAll("validUrls") String url) {
        String shortCode = hashGenerator.generateShortCode(url);
        
        // Verify length is within 6-10 characters (requirement 1.5)
        assertThat(shortCode.length())
            .as("Short code length should be between 6 and 10 characters")
            .isBetween(6, 10);
        
        // Verify only alphanumeric characters are used (requirement 1.6)
        assertThat(shortCode)
            .as("Short code should contain only alphanumeric characters (a-z, A-Z, 0-9)")
            .matches("[a-zA-Z0-9]+");
    }
    
    /**
     * Feature: acortador-urls, Property 1: Short Code Generation Invariants
     * **Validates: Requirements 1.1, 1.5, 1.6**
     * 
     * Random codes should also meet the same format requirements.
     */
    @Property(tries = 10)
    void randomCodesShouldMeetFormatRequirements() {
        String shortCode = hashGenerator.generateRandomCode();
        
        // Verify length is within 6-10 characters (requirement 1.5)
        assertThat(shortCode.length())
            .as("Random short code length should be between 6 and 10 characters")
            .isBetween(6, 10);
        
        // Verify only alphanumeric characters are used (requirement 1.6)
        assertThat(shortCode)
            .as("Random short code should contain only alphanumeric characters (a-z, A-Z, 0-9)")
            .matches("[a-zA-Z0-9]+");
    }
    
    /**
     * Feature: acortador-urls, Property 1: Short Code Generation Invariants
     * **Validates: Requirements 1.1**
     * 
     * Hash-based generation should be deterministic - same URL should produce same code.
     */
    @Property(tries = 10)
    void hashBasedGenerationShouldBeDeterministic(@ForAll("validUrls") String url) {
        String firstCode = hashGenerator.generateShortCode(url);
        String secondCode = hashGenerator.generateShortCode(url);
        
        assertThat(firstCode)
            .as("Same URL should generate the same short code (deterministic)")
            .isEqualTo(secondCode);
    }
    
    /**
     * Feature: acortador-urls, Property 1: Short Code Generation Invariants
     * **Validates: Requirements 1.1**
     * 
     * Random generation should produce different codes (non-deterministic).
     */
    @Property(tries = 10)
    void randomGenerationShouldProduceDifferentCodes() {
        String firstCode = hashGenerator.generateRandomCode();
        String secondCode = hashGenerator.generateRandomCode();
        
        // While theoretically possible to get the same code twice,
        // with 62^7 possibilities, it's extremely unlikely in 100 tries
        assertThat(firstCode)
            .as("Random codes should be different (statistically)")
            .isNotEqualTo(secondCode);
    }
    
    /**
     * Provides valid URL strings for property testing.
     * Generates URLs with various domains, paths, and query parameters.
     */
    @Provide
    Arbitrary<String> validUrls() {
        Arbitrary<String> protocols = Arbitraries.of("http://", "https://");
        Arbitrary<String> domains = Arbitraries.strings()
            .withCharRange('a', 'z')
            .ofMinLength(3)
            .ofMaxLength(15)
            .map(s -> s + ".com");
        Arbitrary<String> paths = Arbitraries.strings()
            .withCharRange('a', 'z')
            .numeric()
            .withChars('/', '-', '_')
            .ofMinLength(0)
            .ofMaxLength(50)
            .map(s -> s.isEmpty() ? "" : "/" + s);
        
        return Combinators.combine(protocols, domains, paths)
            .as((protocol, domain, path) -> protocol + domain + path);
    }
}
