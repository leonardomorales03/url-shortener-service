package com.urlshortener.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA entity representing a click event for analytics tracking.
 * 
 * Requirements:
 * - 5.2: Record the timestamp of each access
 * - 6.1: Store access date and time with millisecond precision
 * - 8.4: Store click events with foreign key relationships and composite index
 */
@Entity
@Table(
    name = "click_events",
    indexes = {
        @Index(name = "idx_short_code_accessed", columnList = "short_code, accessed_at")
    }
)
public class ClickEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "short_code", nullable = false, length = 10)
    private String shortCode;
    
    @Column(name = "accessed_at", nullable = false)
    private LocalDateTime accessedAt;
    
    // Constructors
    
    public ClickEvent() {
    }
    
    public ClickEvent(String shortCode, LocalDateTime accessedAt) {
        this.shortCode = shortCode;
        this.accessedAt = accessedAt;
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
    
    public LocalDateTime getAccessedAt() {
        return accessedAt;
    }
    
    public void setAccessedAt(LocalDateTime accessedAt) {
        this.accessedAt = accessedAt;
    }
    
    @Override
    public String toString() {
        return "ClickEvent{" +
                "id=" + id +
                ", shortCode='" + shortCode + '\'' +
                ", accessedAt=" + accessedAt +
                '}';
    }
}
