package com.urlshortener.service;

import com.urlshortener.dto.ShortenedUrlDto;
import com.urlshortener.exception.InvalidUrlException;
import com.urlshortener.exception.ShortCodeGenerationException;
import com.urlshortener.hash.HashGenerator;
import com.urlshortener.model.UrlMapping;
import com.urlshortener.repository.UrlMappingRepository;
import com.urlshortener.validation.URLValidator;
import com.urlshortener.validation.ValidationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Implementation of URLService for URL shortening operations.
 * 
 * Requirements:
 * - 1.1: Generate unique short code using hash or random algorithm
 * - 1.2: Store mapping between short code and long URL in database
 * - 1.3: Return existing short code for duplicate URLs
 * - 9.1: Generate new code when collision occurs
 * - 9.2: Attempt up to 5 retries when collisions occur
 */
@Service
public class URLServiceImpl implements URLService {
    
    private static final int MAX_COLLISION_RETRIES = 5;
    
    private final UrlMappingRepository urlMappingRepository;
    private final HashGenerator hashGenerator;
    private final URLValidator urlValidator;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    
    @Autowired
    public URLServiceImpl(
            UrlMappingRepository urlMappingRepository,
            HashGenerator hashGenerator,
            URLValidator urlValidator) {
        this.urlMappingRepository = urlMappingRepository;
        this.hashGenerator = hashGenerator;
        this.urlValidator = urlValidator;
    }
    
    @Override
    @Transactional
    public ShortenedUrlDto createShortUrl(String longUrl) {
        // Validate URL
        ValidationResult validationResult = urlValidator.validate(longUrl);
        if (!validationResult.isValid()) {
            throw new InvalidUrlException(
                String.join(", ", validationResult.getErrors())
            );
        }
        
        // Check if URL already exists (idempotent behavior)
        Optional<UrlMapping> existingMapping = urlMappingRepository.findByLongUrl(longUrl);
        if (existingMapping.isPresent()) {
            return mapToDto(existingMapping.get());
        }
        
        // Generate unique short code with collision handling
        String shortCode = generateUniqueShortCode(longUrl);
        
        // Create and save new mapping
        UrlMapping urlMapping = new UrlMapping(
            shortCode,
            longUrl,
            LocalDateTime.now()
        );
        urlMapping = urlMappingRepository.save(urlMapping);
        
        return mapToDto(urlMapping);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<String> getLongUrl(String shortCode) {
        return urlMappingRepository.findByShortCode(shortCode)
            .map(UrlMapping::getLongUrl);
    }
    
    /**
     * Generates a unique short code with collision handling.
     * Tries hash-based generation first, then random codes on collision.
     * 
     * @param longUrl the URL to generate a code for
     * @return a unique short code
     * @throws ShortCodeGenerationException if unable to generate unique code after max retries
     */
    private String generateUniqueShortCode(String longUrl) {
        int attempt = 0;
        
        while (attempt < MAX_COLLISION_RETRIES) {
            String shortCode = attempt == 0 
                ? hashGenerator.generateShortCode(longUrl)
                : hashGenerator.generateRandomCode();
            
            // Check if code already exists
            if (!urlMappingRepository.existsByShortCode(shortCode)) {
                return shortCode;
            }
            
            attempt++;
        }
        
        throw new ShortCodeGenerationException(
            "Unable to generate unique short code after " + MAX_COLLISION_RETRIES + " attempts"
        );
    }
    
    /**
     * Maps a UrlMapping entity to a ShortenedUrlDto.
     * 
     * @param urlMapping the entity to map
     * @return the DTO
     */
    private ShortenedUrlDto mapToDto(UrlMapping urlMapping) {
        String shortUrl = baseUrl + "/" + urlMapping.getShortCode();
        return new ShortenedUrlDto(
            urlMapping.getShortCode(),
            shortUrl,
            urlMapping.getLongUrl(),
            urlMapping.getCreatedAt()
        );
    }
}
