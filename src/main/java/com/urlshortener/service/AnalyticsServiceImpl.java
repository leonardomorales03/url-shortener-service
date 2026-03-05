package com.urlshortener.service;

import com.urlshortener.dto.ClickHistoryPage;
import com.urlshortener.dto.ClickRecord;
import com.urlshortener.dto.UrlStatistics;
import com.urlshortener.exception.ShortUrlNotFoundException;
import com.urlshortener.model.ClickEvent;
import com.urlshortener.model.UrlMapping;
import com.urlshortener.repository.ClickEventRepository;
import com.urlshortener.repository.UrlMappingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of AnalyticsService for tracking and reporting URL analytics.
 * 
 * Requirements:
 * - 5.1: Increment click counter when short URL is accessed
 * - 5.2: Record timestamp of each access
 * - 5.3: Store click events without blocking redirect response
 * - 5.5: Associate each click event with correct short code
 * - 6.1: Store access date and time with millisecond precision
 * - 6.2: Maintain chronological log of access events
 * - 7.1: Return total click count
 * - 7.2: Return click history with timestamps
 * - 7.5: Support pagination for click history
 * - 7.6: Calculate first and last access timestamps
 */
@Service
public class AnalyticsServiceImpl implements AnalyticsService {
    
    private final ClickEventRepository clickEventRepository;
    private final UrlMappingRepository urlMappingRepository;
    
    public AnalyticsServiceImpl(ClickEventRepository clickEventRepository,
                               UrlMappingRepository urlMappingRepository) {
        this.clickEventRepository = clickEventRepository;
        this.urlMappingRepository = urlMappingRepository;
    }
    
    /**
     * Records a click event asynchronously to avoid blocking redirect operations.
     * 
     * @param shortCode the short code that was accessed
     */
    @Override
    @Async
    @Transactional
    public void recordClick(String shortCode) {
        // Verify short code exists
        if (!urlMappingRepository.existsByShortCode(shortCode)) {
            throw new ShortUrlNotFoundException(
                "Short code '" + shortCode + "' does not exist");
        }
        
        ClickEvent clickEvent = new ClickEvent(
            shortCode,
            LocalDateTime.now()
        );
        
        clickEventRepository.save(clickEvent);
    }
    
    /**
     * Retrieves statistics for a short code including total clicks and access timestamps.
     * 
     * @param shortCode the short code to get stats for
     * @return UrlStatistics containing click counts and timestamps
     * @throws ShortUrlNotFoundException if short code doesn't exist
     */
    @Override
    @Transactional(readOnly = true)
    public UrlStatistics getStatistics(String shortCode) {
        UrlMapping urlMapping = urlMappingRepository.findByShortCode(shortCode)
            .orElseThrow(() -> new ShortUrlNotFoundException(
                "Short code '" + shortCode + "' does not exist"));
        
        long totalClicks = clickEventRepository.countByShortCode(shortCode);
        
        LocalDateTime firstAccessAt = clickEventRepository
            .findFirstByShortCodeOrderByAccessedAtAsc(shortCode)
            .map(ClickEvent::getAccessedAt)
            .orElse(null);
        
        LocalDateTime lastAccessAt = clickEventRepository
            .findFirstByShortCodeOrderByAccessedAtDesc(shortCode)
            .map(ClickEvent::getAccessedAt)
            .orElse(null);
        
        return new UrlStatistics(
            shortCode,
            urlMapping.getLongUrl(),
            totalClicks,
            urlMapping.getCreatedAt(),
            firstAccessAt,
            lastAccessAt
        );
    }
    
    /**
     * Retrieves paginated click history for a short code.
     * Results are ordered by timestamp descending (most recent first).
     * 
     * @param shortCode the short code to get history for
     * @param page page number (0-indexed)
     * @param size number of records per page
     * @return ClickHistoryPage containing click events
     * @throws ShortUrlNotFoundException if short code doesn't exist
     */
    @Override
    @Transactional(readOnly = true)
    public ClickHistoryPage getClickHistory(String shortCode, int page, int size) {
        // Verify short code exists
        if (!urlMappingRepository.existsByShortCode(shortCode)) {
            throw new ShortUrlNotFoundException(
                "Short code '" + shortCode + "' does not exist");
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "accessedAt"));
        Page<ClickEvent> clickEventPage = clickEventRepository.findByShortCode(shortCode, pageable);
        
        List<ClickRecord> clicks = clickEventPage.getContent().stream()
            .map(event -> new ClickRecord(event.getAccessedAt()))
            .collect(Collectors.toList());
        
        long totalClicks = clickEventRepository.countByShortCode(shortCode);
        
        return new ClickHistoryPage(
            shortCode,
            totalClicks,
            clicks,
            page,
            size,
            clickEventPage.getTotalPages()
        );
    }
}
