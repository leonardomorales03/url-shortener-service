package com.urlshortener.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * DTO representing a single click record.
 * 
 * Requirements:
 * - 6.1: Store access date and time with millisecond precision
 * - 7.2: Return click history with timestamps
 */
public class ClickRecord {
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime timestamp;
    
    // Constructors
    
    public ClickRecord() {
    }
    
    public ClickRecord(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    // Getters and Setters
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
