package com.urlshortener.dto;

import java.util.List;

/**
 * DTO for paginated click history.
 * 
 * Requirements:
 * - 7.2: Return click history with timestamps
 * - 7.5: Support pagination for large datasets
 */
public class ClickHistoryPage {
    private String shortCode;
    private long totalClicks;
    private List<ClickRecord> clicks;
    private int page;
    private int size;
    private int totalPages;
    
    // Constructors
    
    public ClickHistoryPage() {
    }
    
    public ClickHistoryPage(String shortCode, long totalClicks, List<ClickRecord> clicks, 
                           int page, int size, int totalPages) {
        this.shortCode = shortCode;
        this.totalClicks = totalClicks;
        this.clicks = clicks;
        this.page = page;
        this.size = size;
        this.totalPages = totalPages;
    }
    
    // Getters and Setters
    
    public String getShortCode() {
        return shortCode;
    }
    
    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }
    
    public long getTotalClicks() {
        return totalClicks;
    }
    
    public void setTotalClicks(long totalClicks) {
        this.totalClicks = totalClicks;
    }
    
    public List<ClickRecord> getClicks() {
        return clicks;
    }
    
    public void setClicks(List<ClickRecord> clicks) {
        this.clicks = clicks;
    }
    
    public int getPage() {
        return page;
    }
    
    public void setPage(int page) {
        this.page = page;
    }
    
    public int getSize() {
        return size;
    }
    
    public void setSize(int size) {
        this.size = size;
    }
    
    public int getTotalPages() {
        return totalPages;
    }
    
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}
