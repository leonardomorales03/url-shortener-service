package com.urlshortener.service;

import com.urlshortener.dto.RedirectResponse;
import com.urlshortener.exception.InvalidUrlException;
import com.urlshortener.exception.ShortUrlNotFoundException;
import com.urlshortener.validation.URLValidator;
import com.urlshortener.validation.ValidationResult;
import org.springframework.stereotype.Service;

/**
 * Implementation of RedirectService that handles URL redirects.
 * Uses URLService to lookup URLs and URLValidator to validate stored URLs.
 * Integrates with AnalyticsService to record click events.
 */
@Service
public class RedirectServiceImpl implements RedirectService {
    
    private static final int HTTP_MOVED_PERMANENTLY = 301;
    
    private final URLService urlService;
    private final URLValidator urlValidator;
    private final AnalyticsService analyticsService;

    public RedirectServiceImpl(URLService urlService, URLValidator urlValidator, 
                              AnalyticsService analyticsService) {
        this.urlService = urlService;
        this.urlValidator = urlValidator;
        this.analyticsService = analyticsService;
    }

    @Override
    public RedirectResponse handleRedirect(String shortCode) {
        // Lookup URL by short code
        String longUrl = urlService.getLongUrl(shortCode)
            .orElseThrow(() -> new ShortUrlNotFoundException(
                shortCode, 
                "The short code does not exist"
            ));
        
        // Validate stored URL format before redirecting
        ValidationResult validationResult = urlValidator.validate(longUrl);
        if (!validationResult.isValid()) {
            throw new InvalidUrlException(
                "Stored URL is invalid: " + String.join(", ", validationResult.getErrors())
            );
        }
        
        // Record click event asynchronously (doesn't block redirect response)
        // Analytics failures are handled gracefully and don't affect the redirect
        try {
            analyticsService.recordClick(shortCode);
        } catch (Exception e) {
            // Log error but don't fail the redirect
            // The @Async annotation ensures this runs in a separate thread
            // so exceptions here won't affect the redirect response
        }
        
        // Return redirect response with HTTP 301 status
        return new RedirectResponse(longUrl, HTTP_MOVED_PERMANENTLY);
    }
}
