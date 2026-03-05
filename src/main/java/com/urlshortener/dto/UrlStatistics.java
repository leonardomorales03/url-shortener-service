package com.urlshortener.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * DTO for URL statistics.
 * 
 * Requirements:
 * - 7.1: Return total click count
 * - 7.6: Return first and last access timestamps
 */
public class UrlStatistics {
    private String shortCode;
    private String originalUrl;
    private long totalClicks;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime firstAccessAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime lastAccessAt;
    
    // Constructors
    
    public UrlStatistics() {
    }
    
    public UrlStatistics(String shortCode, String originalUrl, long totalClicks, 
                        LocalDateTime createdAt, LocalDateTime firstAccessAt, 
                        LocalDateTime lastAccessAt) {
        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
        this.totalClicks = totalClicks;
        this.createdAt = createdAt;
        this.firstAccessAt = firstAccessAt;
        this.lastAccessAt = lastAccessAt;
    }
    
    // Getters and Setters
    
    public String getShortCode() {
        return shortCode;
    }
    
    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }
    
    public String getOriginalUrl() {
        return originalUrl;
    }
    
    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }
    
    public long getTotalClicks() {
        return totalClicks;
    }
    
    public void setTotalClicks(long totalClicks) {
        this.totalClicks = totalClicks;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getFirstAccessAt() {
        return firstAccessAt;
    }
    
    public void setFirstAccessAt(LocalDateTime firstAccessAt) {
        this.firstAccessAt = firstAccessAt;
    }
    
    public LocalDateTime getLastAccessAt() {
        return lastAccessAt;
    }
    
    public void setLastAccessAt(LocalDateTime lastAccessAt) {
        this.lastAccessAt = lastAccessAt;
    }
}
