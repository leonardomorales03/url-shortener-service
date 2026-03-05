package com.urlshortener.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing the mapping between a short code and a long URL.
 * 
 * Requirements:
 * - 1.2: Store the mapping between short code and long URL
 * - 8.3: Store URL mappings with appropriate indexes for fast lookups
 */
@Entity
@Table(
    name = "url_mappings",
    indexes = {
        @Index(name = "idx_short_code", columnList = "short_code")
    }
)
public class UrlMapping {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "short_code", unique = true, nullable = false, length = 10)
    private String shortCode;
    
    @Column(name = "long_url", nullable = false, length = 2048)
    private String longUrl;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    
    public UrlMapping() {
    }
    
    public UrlMapping(String shortCode, String longUrl, LocalDateTime createdAt) {
        this.shortCode = shortCode;
        this.longUrl = longUrl;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getShortCode() {
        return shortCode;
    }
    
    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }
    
    public String getLongUrl() {
        return longUrl;
    }
    
    public void setLongUrl(String longUrl) {
        this.longUrl = longUrl;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "UrlMapping{" +
                "id=" + id +
                ", shortCode='" + shortCode + '\'' +
                ", longUrl='" + longUrl + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
