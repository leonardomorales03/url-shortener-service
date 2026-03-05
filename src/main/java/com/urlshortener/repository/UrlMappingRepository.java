package com.urlshortener.repository;

import com.urlshortener.model.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for UrlMapping entities.
 * 
 * Requirements:
 * - 1.2: Find URL mappings by short code
 * - 7.5: Support database operations for URL mappings
 */
@Repository
public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {
    
    /**
     * Find a URL mapping by its short code.
     * 
     * @param shortCode the short code to search for
     * @return Optional containing the UrlMapping if found
     */
    Optional<UrlMapping> findByShortCode(String shortCode);
    
    /**
     * Check if a URL mapping exists with the given short code.
     * 
     * @param shortCode the short code to check
     * @return true if a mapping exists, false otherwise
     */
    boolean existsByShortCode(String shortCode);
    
    /**
     * Find a URL mapping by its long URL.
     * 
     * @param longUrl the long URL to search for
     * @return Optional containing the UrlMapping if found
     */
    Optional<UrlMapping> findByLongUrl(String longUrl);
}
