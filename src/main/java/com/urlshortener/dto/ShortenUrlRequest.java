package com.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for URL shortening requests.
 * 
 * Requirements:
 * - 2.1: Accept long URL in request body
 * - 2.4: Accept URLs in JSON format with defined schema
 */
public class ShortenUrlRequest {
    
    @NotBlank(message = "URL cannot be empty")
    @Size(max = 2048, message = "URL exceeds maximum length of 2048 characters")
    private String url;
    
    public ShortenUrlRequest() {
    }
    
    public ShortenUrlRequest(String url) {
        this.url = url;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
}
