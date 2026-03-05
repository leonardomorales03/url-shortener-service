package com.urlshortener.service;

import com.urlshortener.dto.ClickHistoryPage;
import com.urlshortener.dto.UrlStatistics;

/**
 * Service interface for analytics operations.
 * 
 * Requirements:
 * - 5.1: Increment click counter when short URL is accessed
 * - 5.2: Record timestamp of each access
 * - 6.1: Store access date and time with millisecond precision
 * - 6.2: Maintain chronological log of access events
 * - 7.1: Return total click count for a short code
 * - 7.2: Return click history with timestamps
 * - 7.5: Support pagination for click history
 * - 7.6: Calculate and return first and last access dates
 */
public interface AnalyticsService {
    /**
     * Records a click event for a short code.
     * This operation is asynchronous to avoid blocking redirect operations.
     * 
     * @param shortCode the short code that was accessed
     */
    void recordClick(String shortCode);
    
    /**
     * Retrieves statistics for a short code.
     * 
     * @param shortCode the short code to get stats for
     * @return UrlStatistics containing click counts and timestamps
     * @throws com.urlshortener.exception.ShortUrlNotFoundException if short code doesn't exist
     */
    UrlStatistics getStatistics(String shortCode);
    
    /**
     * Retrieves paginated click history for a short code.
     * 
     * @param shortCode the short code to get history for
     * @param page page number (0-indexed)
     * @param size number of records per page
     * @return ClickHistoryPage containing click events
     * @throws com.urlshortener.exception.ShortUrlNotFoundException if short code doesn't exist
     */
    ClickHistoryPage getClickHistory(String shortCode, int page, int size);
}
