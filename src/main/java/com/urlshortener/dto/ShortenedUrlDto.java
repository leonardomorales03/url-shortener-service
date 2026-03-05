package com.urlshortener.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * DTO representing a shortened URL response.
 * 
 * Requirements:
 * - 2.2: Return short code with HTTP status 201
 * - 2.5: Include both short code and full shortened URL
 */
public class ShortenedUrlDto {
    private String shortCode;
    private String shortUrl;
    private String originalUrl;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;
    
    public ShortenedUrlDto() {
    }
    
    public ShortenedUrlDto(String shortCode, String shortUrl, String originalUrl, LocalDateTime createdAt) {
        this.shortCode = shortCode;
        this.shortUrl = shortUrl;
        this.originalUrl = originalUrl;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    
    public String getShortCode() {
        return shortCode;
    }
    
    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }
    
    public String getShortUrl() {
        return shortUrl;
    }
    
    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
    }
    
    public String getOriginalUrl() {
        return originalUrl;
    }
    
    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
