package com.urlshortener.service;

import com.urlshortener.dto.ClickHistoryPage;
import com.urlshortener.dto.ClickRecord;
import com.urlshortener.dto.ShortenedUrlDto;
import com.urlshortener.dto.UrlStatistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for AnalyticsService using JUnit's @RepeatedTest.
 * These tests verify universal properties across many randomized inputs.
 */
@SpringBootTest
@ActiveProfiles("test")
class AnalyticsServicePropertyTest {
    
    @Autowired
    private AnalyticsService analyticsService;
    
    @Autowired
    private URLService urlService;
    
    private final SecureRandom random = new SecureRandom();
    
    @BeforeEach
    void setUp() {
        // Database is cleaned between tests due to @Transactional
    }
    
    /**
     * Feature: acortador-urls, Property 9: Click Event Recording
     * **Validates: Requirements 3.4, 5.1, 5.2, 5.5**
     * 
     * For any successful redirect, a click event should be recorded with the correct 
     * short code, an incremented click counter, and a timestamp.
     */
    @RepeatedTest(10)
    void clickEventsShouldBeRecordedWithCorrectShortCodeAndTimestamp() throws InterruptedException {
        // Generate a random valid URL and create a short code
        String url = generateRandomValidUrl();
        ShortenedUrlDto shortened = urlService.createShortUrl(url);
        String shortCode = shortened.getShortCode();
        
        // Record a random number of clicks (1-10)
        int clickCount = 1 + random.nextInt(10);
        LocalDateTime beforeFirstClick = LocalDateTime.now();
        
        for (int i = 0; i < clickCount; i++) {
            analyticsService.recordClick(shortCode);
            // Small delay to ensure async processing completes
            Thread.sleep(10);
        }
        
        // Wait for async processing to complete
        Thread.sleep(100);
        
        LocalDateTime afterLastClick = LocalDateTime.now();
        
        // Verify statistics
        UrlStatistics stats = analyticsService.getStatistics(shortCode);
        
        assertThat(stats.getShortCode())
            .as("Statistics should be for the correct short code")
            .isEqualTo(shortCode);
        
        assertThat(stats.getTotalClicks())
            .as("Total clicks should match the number of recorded clicks")
            .isEqualTo(clickCount);
        
        assertThat(stats.getFirstAccessAt())
            .as("First access timestamp should be recorded")
            .isNotNull()
            .isAfterOrEqualTo(beforeFirstClick)
            .isBeforeOrEqualTo(afterLastClick);
        
        assertThat(stats.getLastAccessAt())
            .as("Last access timestamp should be recorded")
            .isNotNull()
            .isAfterOrEqualTo(stats.getFirstAccessAt())
            .isBeforeOrEqualTo(afterLastClick);
        
        // Verify click history contains all events
        ClickHistoryPage history = analyticsService.getClickHistory(shortCode, 0, 100);
        
        assertThat(history.getClicks())
            .as("Click history should contain all recorded clicks")
            .hasSize(clickCount);
        
        assertThat(history.getTotalClicks())
            .as("Total clicks in history should match statistics")
            .isEqualTo(clickCount);
    }
    
    /**
     * Feature: acortador-urls, Property 10: Chronological Click Logging
     * **Validates: Requirements 6.1, 6.2, 6.5**
     * 
     * For any sequence of click events for a short code, when querying the access logs, 
     * the results should be ordered chronologically by timestamp with millisecond precision.
     */
    @RepeatedTest(10)
    void clickHistoryShouldBeOrderedChronologically() throws InterruptedException {
        // Generate a random valid URL and create a short code
        String url = generateRandomValidUrl();
        ShortenedUrlDto shortened = urlService.createShortUrl(url);
        String shortCode = shortened.getShortCode();
        
        // Record multiple clicks with delays to ensure different timestamps
        int clickCount = 5 + random.nextInt(10); // 5-14 clicks
        List<LocalDateTime> recordedTimes = new ArrayList<>();
        
        for (int i = 0; i < clickCount; i++) {
            LocalDateTime beforeClick = LocalDateTime.now();
            analyticsService.recordClick(shortCode);
            recordedTimes.add(beforeClick);
            Thread.sleep(10); // Ensure different timestamps
        }
        
        // Wait for async processing to complete
        Thread.sleep(100);
        
        // Retrieve click history
        ClickHistoryPage history = analyticsService.getClickHistory(shortCode, 0, 100);
        List<ClickRecord> clicks = history.getClicks();
        
        assertThat(clicks)
            .as("Click history should contain all recorded clicks")
            .hasSize(clickCount);
        
        // Verify chronological order (descending - most recent first)
        for (int i = 0; i < clicks.size() - 1; i++) {
            LocalDateTime current = clicks.get(i).getTimestamp();
            LocalDateTime next = clicks.get(i + 1).getTimestamp();
            
            assertThat(current)
                .as("Clicks should be ordered by timestamp descending (most recent first)")
                .isAfterOrEqualTo(next);
        }
        
        // Verify all timestamps are not null and have precision
        for (ClickRecord click : clicks) {
            assertThat(click.getTimestamp())
                .as("Each click should have a timestamp")
                .isNotNull();
        }
    }
    
    /**
     * Feature: acortador-urls, Property 11: Statistics Calculation Accuracy
     * **Validates: Requirements 7.6**
     * 
     * For any short code with recorded clicks, the statistics should accurately report 
     * the total click count, first access timestamp, and most recent access timestamp 
     * based on the click events.
     */
    @RepeatedTest(10)
    void statisticsShouldAccuratelyReflectClickEvents() throws InterruptedException {
        // Generate a random valid URL and create a short code
        String url = generateRandomValidUrl();
        ShortenedUrlDto shortened = urlService.createShortUrl(url);
        String shortCode = shortened.getShortCode();
        
        // Record multiple clicks
        int clickCount = 3 + random.nextInt(8); // 3-10 clicks
        
        for (int i = 0; i < clickCount; i++) {
            analyticsService.recordClick(shortCode);
            Thread.sleep(10);
        }
        
        // Wait for async processing
        Thread.sleep(100);
        
        // Get statistics and click history
        UrlStatistics stats = analyticsService.getStatistics(shortCode);
        ClickHistoryPage history = analyticsService.getClickHistory(shortCode, 0, 100);
        
        // Verify total clicks accuracy
        assertThat(stats.getTotalClicks())
            .as("Statistics total clicks should match actual click count")
            .isEqualTo(clickCount);
        
        assertThat(history.getTotalClicks())
            .as("History total clicks should match statistics")
            .isEqualTo(stats.getTotalClicks());
        
        // Verify first and last access timestamps match the actual events
        List<ClickRecord> clicks = history.getClicks();
        
        if (!clicks.isEmpty()) {
            // Most recent click (first in descending order)
            LocalDateTime mostRecentInHistory = clicks.get(0).getTimestamp();
            assertThat(stats.getLastAccessAt())
                .as("Last access timestamp should match most recent click in history")
                .isEqualTo(mostRecentInHistory);
            
            // Oldest click (last in descending order)
            LocalDateTime oldestInHistory = clicks.get(clicks.size() - 1).getTimestamp();
            assertThat(stats.getFirstAccessAt())
                .as("First access timestamp should match oldest click in history")
                .isEqualTo(oldestInHistory);
        }
        
        // Verify first access is before or equal to last access
        if (stats.getFirstAccessAt() != null && stats.getLastAccessAt() != null) {
            assertThat(stats.getFirstAccessAt())
                .as("First access should be before or equal to last access")
                .isBeforeOrEqualTo(stats.getLastAccessAt());
        }
    }
    
    /**
     * Feature: acortador-urls, Property 12: Pagination Consistency
     * **Validates: Requirements 7.5**
     * 
     * For any short code with click history, paginating through all pages should return 
     * all click events exactly once without duplicates or omissions.
     */
    @RepeatedTest(10)
    void paginationShouldReturnAllClicksWithoutDuplicatesOrOmissions() throws InterruptedException {
        // Generate a random valid URL and create a short code
        String url = generateRandomValidUrl();
        ShortenedUrlDto shortened = urlService.createShortUrl(url);
        String shortCode = shortened.getShortCode();
        
        // Record a random number of clicks (10-30 to test pagination)
        int clickCount = 10 + random.nextInt(21);
        
        for (int i = 0; i < clickCount; i++) {
            analyticsService.recordClick(shortCode);
            Thread.sleep(5);
        }
        
        // Wait for async processing
        Thread.sleep(100);
        
        // Choose a random page size (3-7)
        int pageSize = 3 + random.nextInt(5);
        
        // Collect all clicks from all pages
        Set<LocalDateTime> allTimestamps = new HashSet<>();
        int totalPagesVisited = 0;
        int totalClicksCollected = 0;
        
        ClickHistoryPage firstPage = analyticsService.getClickHistory(shortCode, 0, pageSize);
        int expectedTotalPages = firstPage.getTotalPages();
        
        for (int page = 0; page < expectedTotalPages; page++) {
            ClickHistoryPage historyPage = analyticsService.getClickHistory(shortCode, page, pageSize);
            
            assertThat(historyPage.getPage())
                .as("Page number should match requested page")
                .isEqualTo(page);
            
            assertThat(historyPage.getSize())
                .as("Page size should match requested size")
                .isEqualTo(pageSize);
            
            assertThat(historyPage.getTotalPages())
                .as("Total pages should be consistent across all requests")
                .isEqualTo(expectedTotalPages);
            
            assertThat(historyPage.getTotalClicks())
                .as("Total clicks should be consistent across all pages")
                .isEqualTo(clickCount);
            
            // Collect timestamps from this page
            for (ClickRecord click : historyPage.getClicks()) {
                boolean isNew = allTimestamps.add(click.getTimestamp());
                assertThat(isNew)
                    .as("Each click should appear exactly once (no duplicates)")
                    .isTrue();
            }
            
            totalClicksCollected += historyPage.getClicks().size();
            totalPagesVisited++;
        }
        
        // Verify we visited all pages
        assertThat(totalPagesVisited)
            .as("Should visit all pages")
            .isEqualTo(expectedTotalPages);
        
        // Verify we collected all clicks
        assertThat(totalClicksCollected)
            .as("Should collect all clicks across all pages (no omissions)")
            .isEqualTo(clickCount);
        
        assertThat(allTimestamps)
            .as("Should have unique timestamps for all clicks")
            .hasSize(clickCount);
    }
    
    /**
     * Generates a random valid URL for testing.
     * Format: (http|https)://[domain].com/[path]
     */
    private String generateRandomValidUrl() {
        String protocol = random.nextBoolean() ? "http://" : "https://";
        String domain = generateRandomString(3, 15, "abcdefghijklmnopqrstuvwxyz") + ".com";
        String path = random.nextBoolean() ? "" : "/" + generateRandomString(0, 50, "abcdefghijklmnopqrstuvwxyz0123456789/-_");
        return protocol + domain + path;
    }
    
    /**
     * Generates a random string with characters from the given alphabet.
     */
    private String generateRandomString(int minLength, int maxLength, String alphabet) {
        int length = minLength + random.nextInt(maxLength - minLength + 1);
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return sb.toString();
    }
}
