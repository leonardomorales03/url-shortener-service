package com.urlshortener.service;

import com.urlshortener.dto.ClickHistoryPage;
import com.urlshortener.dto.ShortenedUrlDto;
import com.urlshortener.dto.UrlStatistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for AnalyticsService edge cases.
 * 
 * Requirements:
 * - 7.5: Support pagination for click history
 * - 7.6: Calculate and return first and last access dates
 */
@SpringBootTest
@ActiveProfiles("test")
class AnalyticsServiceTest {
    
    @Autowired
    private AnalyticsService analyticsService;
    
    @Autowired
    private URLService urlService;
    
    /**
     * Test statistics for URL with no clicks.
     * Requirements: 7.6
     */
    @Test
    void shouldReturnZeroClicksForUrlWithNoClicks() {
        // Create a short URL but don't record any clicks
        String url = "https://example.com/no-clicks";
        ShortenedUrlDto shortened = urlService.createShortUrl(url);
        String shortCode = shortened.getShortCode();
        
        // Get statistics
        UrlStatistics stats = analyticsService.getStatistics(shortCode);
        
        assertThat(stats.getTotalClicks()).isEqualTo(0);
        assertThat(stats.getFirstAccessAt()).isNull();
        assertThat(stats.getLastAccessAt()).isNull();
        assertThat(stats.getShortCode()).isEqualTo(shortCode);
        assertThat(stats.getOriginalUrl()).isEqualTo(url);
    }
    
    /**
     * Test pagination with various page sizes.
     * Requirements: 7.5
     */
    @Test
    void shouldHandlePaginationWithVariousPageSizes() throws InterruptedException {
        // Create a short URL and record 15 clicks
        String url = "https://example.com/pagination-test";
        ShortenedUrlDto shortened = urlService.createShortUrl(url);
        String shortCode = shortened.getShortCode();
        
        int totalClicks = 15;
        for (int i = 0; i < totalClicks; i++) {
            analyticsService.recordClick(shortCode);
            Thread.sleep(5);
        }
        
        // Wait for async processing
        Thread.sleep(100);
        
        // Test with page size 5
        ClickHistoryPage page1 = analyticsService.getClickHistory(shortCode, 0, 5);
        assertThat(page1.getClicks()).hasSize(5);
        assertThat(page1.getTotalClicks()).isEqualTo(totalClicks);
        assertThat(page1.getTotalPages()).isEqualTo(3);
        assertThat(page1.getPage()).isEqualTo(0);
        assertThat(page1.getSize()).isEqualTo(5);
        
        ClickHistoryPage page2 = analyticsService.getClickHistory(shortCode, 1, 5);
        assertThat(page2.getClicks()).hasSize(5);
        assertThat(page2.getPage()).isEqualTo(1);
        
        ClickHistoryPage page3 = analyticsService.getClickHistory(shortCode, 2, 5);
        assertThat(page3.getClicks()).hasSize(5);
        assertThat(page3.getPage()).isEqualTo(2);
        
        // Test with page size 10
        ClickHistoryPage largePage1 = analyticsService.getClickHistory(shortCode, 0, 10);
        assertThat(largePage1.getClicks()).hasSize(10);
        assertThat(largePage1.getTotalPages()).isEqualTo(2);
        
        ClickHistoryPage largePage2 = analyticsService.getClickHistory(shortCode, 1, 10);
        assertThat(largePage2.getClicks()).hasSize(5);
        
        // Test with page size 1
        ClickHistoryPage smallPage = analyticsService.getClickHistory(shortCode, 0, 1);
        assertThat(smallPage.getClicks()).hasSize(1);
        assertThat(smallPage.getTotalPages()).isEqualTo(15);
        
        // Test with page size larger than total clicks
        ClickHistoryPage bigPage = analyticsService.getClickHistory(shortCode, 0, 100);
        assertThat(bigPage.getClicks()).hasSize(totalClicks);
        assertThat(bigPage.getTotalPages()).isEqualTo(1);
    }
    
    /**
     * Test first and last access timestamp calculations.
     * Requirements: 7.6
     */
    @Test
    void shouldCorrectlyCalculateFirstAndLastAccessTimestamps() throws InterruptedException {
        // Create a short URL
        String url = "https://example.com/timestamp-test";
        ShortenedUrlDto shortened = urlService.createShortUrl(url);
        String shortCode = shortened.getShortCode();
        
        // Record first click
        analyticsService.recordClick(shortCode);
        Thread.sleep(50);
        
        // Record second click
        analyticsService.recordClick(shortCode);
        Thread.sleep(50);
        
        // Record third click
        analyticsService.recordClick(shortCode);
        Thread.sleep(100);
        
        // Get statistics
        UrlStatistics stats = analyticsService.getStatistics(shortCode);
        
        assertThat(stats.getTotalClicks()).isEqualTo(3);
        assertThat(stats.getFirstAccessAt()).isNotNull();
        assertThat(stats.getLastAccessAt()).isNotNull();
        
        // First access should be before last access
        assertThat(stats.getFirstAccessAt()).isBefore(stats.getLastAccessAt());
        
        // Get click history to verify timestamps match
        ClickHistoryPage history = analyticsService.getClickHistory(shortCode, 0, 10);
        assertThat(history.getClicks()).hasSize(3);
        
        // Most recent click (first in descending order) should match lastAccessAt
        assertThat(history.getClicks().get(0).getTimestamp()).isEqualTo(stats.getLastAccessAt());
        
        // Oldest click (last in descending order) should match firstAccessAt
        assertThat(history.getClicks().get(2).getTimestamp()).isEqualTo(stats.getFirstAccessAt());
    }
    
    /**
     * Test pagination with empty results.
     * Requirements: 7.5
     */
    @Test
    void shouldHandleEmptyPaginationResults() {
        // Create a short URL with no clicks
        String url = "https://example.com/empty-pagination";
        ShortenedUrlDto shortened = urlService.createShortUrl(url);
        String shortCode = shortened.getShortCode();
        
        // Get first page
        ClickHistoryPage page = analyticsService.getClickHistory(shortCode, 0, 10);
        
        assertThat(page.getClicks()).isEmpty();
        assertThat(page.getTotalClicks()).isEqualTo(0);
        assertThat(page.getTotalPages()).isEqualTo(0);
        assertThat(page.getPage()).isEqualTo(0);
        assertThat(page.getSize()).isEqualTo(10);
    }
    
    /**
     * Test pagination beyond available pages.
     * Requirements: 7.5
     */
    @Test
    void shouldHandlePaginationBeyondAvailablePages() throws InterruptedException {
        // Create a short URL with 5 clicks
        String url = "https://example.com/beyond-pages";
        ShortenedUrlDto shortened = urlService.createShortUrl(url);
        String shortCode = shortened.getShortCode();
        
        for (int i = 0; i < 5; i++) {
            analyticsService.recordClick(shortCode);
            Thread.sleep(5);
        }
        
        Thread.sleep(100);
        
        // Request page 10 (beyond available pages)
        ClickHistoryPage page = analyticsService.getClickHistory(shortCode, 10, 10);
        
        assertThat(page.getClicks()).isEmpty();
        assertThat(page.getTotalClicks()).isEqualTo(5);
        assertThat(page.getTotalPages()).isEqualTo(1);
        assertThat(page.getPage()).isEqualTo(10);
    }
}
