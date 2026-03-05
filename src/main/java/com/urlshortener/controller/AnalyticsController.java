package com.urlshortener.controller;

import com.urlshortener.dto.ClickHistoryPage;
import com.urlshortener.dto.ErrorResponse;
import com.urlshortener.dto.UrlStatistics;
import com.urlshortener.exception.ShortUrlNotFoundException;
import com.urlshortener.service.AnalyticsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for analytics endpoints.
 * 
 * Requirements:
 * - 7.1: Expose endpoint that returns total click count
 * - 7.2: Expose endpoint that returns click history with timestamps
 * - 7.3: Return HTTP 404 for non-existent short codes
 * - 7.4: Return statistics in JSON format
 * - 7.5: Support pagination for click history
 * - 7.6: Return first and last access dates
 */
@RestController
@RequestMapping("/api/stats")
public class AnalyticsController {
    
    private final AnalyticsService analyticsService;
    
    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }
    
    /**
     * Get statistics for a short code.
     * 
     * @param shortCode the short code to get statistics for
     * @return UrlStatistics with total clicks and timestamps
     */
    @GetMapping("/{shortCode}")
    public ResponseEntity<UrlStatistics> getStatistics(@PathVariable String shortCode) {
        UrlStatistics statistics = analyticsService.getStatistics(shortCode);
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * Get paginated click history for a short code.
     * 
     * @param shortCode the short code to get history for
     * @param page page number (0-indexed), defaults to 0
     * @param size number of records per page, defaults to 20
     * @return ClickHistoryPage with paginated click events
     */
    @GetMapping("/{shortCode}/history")
    public ResponseEntity<ClickHistoryPage> getClickHistory(
            @PathVariable String shortCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        ClickHistoryPage history = analyticsService.getClickHistory(shortCode, page, size);
        return ResponseEntity.ok(history);
    }
    
    /**
     * Exception handler for ShortUrlNotFoundException.
     * Returns 404 Not Found with error details.
     */
    @ExceptionHandler(ShortUrlNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleShortUrlNotFound(
            ShortUrlNotFoundException ex,
            HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
            "Short URL not found",
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
}
