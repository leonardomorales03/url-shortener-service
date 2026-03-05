package com.urlshortener.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urlshortener.dto.ShortenUrlRequest;
import com.urlshortener.dto.ShortenedUrlDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AnalyticsController.
 * 
 * Requirements:
 * - 7.1: Expose endpoint that returns total click count
 * - 7.2: Expose endpoint that returns click history with timestamps
 * - 7.3: Return HTTP 404 for non-existent short codes
 * - 7.4: Return statistics in JSON format
 * - 7.5: Support pagination for click history
 * - 7.6: Return first and last access dates
 */
@SpringBootTest
@AutoConfigureMockMvc
class AnalyticsControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void shouldReturnStatisticsForValidShortCode() throws Exception {
        // Create a short URL
        String longUrl = "https://example.com/stats-test";
        ShortenUrlRequest request = new ShortenUrlRequest(longUrl);
        
        MvcResult createResult = mockMvc.perform(post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();
        
        ShortenedUrlDto response = objectMapper.readValue(
            createResult.getResponse().getContentAsString(),
            ShortenedUrlDto.class
        );
        
        String shortCode = response.getShortCode();
        
        // Access the short URL to generate clicks
        mockMvc.perform(get("/" + shortCode))
            .andExpect(status().isMovedPermanently());
        
        // Get statistics
        mockMvc.perform(get("/api/stats/" + shortCode))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.shortCode").value(shortCode))
            .andExpect(jsonPath("$.originalUrl").value(longUrl))
            .andExpect(jsonPath("$.totalClicks").value(1))
            .andExpect(jsonPath("$.createdAt").exists())
            .andExpect(jsonPath("$.firstAccessAt").exists())
            .andExpect(jsonPath("$.lastAccessAt").exists());
    }
    
    @Test
    void shouldReturn404ForNonExistentShortCodeInStats() throws Exception {
        String nonExistentCode = "invalid";
        
        mockMvc.perform(get("/api/stats/" + nonExistentCode))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Short URL not found"))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/stats/" + nonExistentCode));
    }
    
    @Test
    void shouldReturnClickHistoryWithPagination() throws Exception {
        // Create a short URL
        String longUrl = "https://example.com/history-test";
        ShortenUrlRequest request = new ShortenUrlRequest(longUrl);
        
        MvcResult createResult = mockMvc.perform(post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();
        
        ShortenedUrlDto response = objectMapper.readValue(
            createResult.getResponse().getContentAsString(),
            ShortenedUrlDto.class
        );
        
        String shortCode = response.getShortCode();
        
        // Generate multiple clicks
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/" + shortCode))
                .andExpect(status().isMovedPermanently());
            Thread.sleep(10); // Small delay to ensure different timestamps
        }
        
        // Get click history with default pagination
        mockMvc.perform(get("/api/stats/" + shortCode + "/history"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.shortCode").value(shortCode))
            .andExpect(jsonPath("$.totalClicks").value(5))
            .andExpect(jsonPath("$.clicks").isArray())
            .andExpect(jsonPath("$.clicks.length()").value(5))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(20))
            .andExpect(jsonPath("$.totalPages").value(1));
    }
    
    @Test
    void shouldSupportCustomPaginationParameters() throws Exception {
        // Create a short URL
        String longUrl = "https://example.com/pagination-test";
        ShortenUrlRequest request = new ShortenUrlRequest(longUrl);
        
        MvcResult createResult = mockMvc.perform(post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();
        
        ShortenedUrlDto response = objectMapper.readValue(
            createResult.getResponse().getContentAsString(),
            ShortenedUrlDto.class
        );
        
        String shortCode = response.getShortCode();
        
        // Generate multiple clicks
        for (int i = 0; i < 15; i++) {
            mockMvc.perform(get("/" + shortCode))
                .andExpect(status().isMovedPermanently());
        }
        
        // Get first page with size 10
        mockMvc.perform(get("/api/stats/" + shortCode + "/history?page=0&size=10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.clicks.length()").value(10))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(10))
            .andExpect(jsonPath("$.totalPages").value(2));
        
        // Get second page with size 10
        mockMvc.perform(get("/api/stats/" + shortCode + "/history?page=1&size=10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.clicks.length()").value(5))
            .andExpect(jsonPath("$.page").value(1))
            .andExpect(jsonPath("$.size").value(10));
    }
    
    @Test
    void shouldReturn404ForNonExistentShortCodeInHistory() throws Exception {
        String nonExistentCode = "invalid";
        
        mockMvc.perform(get("/api/stats/" + nonExistentCode + "/history"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Short URL not found"))
            .andExpect(jsonPath("$.message").exists());
    }
    
    @Test
    void shouldReturnZeroClicksForNewShortUrl() throws Exception {
        // Create a short URL without accessing it
        String longUrl = "https://example.com/zero-clicks";
        ShortenUrlRequest request = new ShortenUrlRequest(longUrl);
        
        MvcResult createResult = mockMvc.perform(post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();
        
        ShortenedUrlDto response = objectMapper.readValue(
            createResult.getResponse().getContentAsString(),
            ShortenedUrlDto.class
        );
        
        String shortCode = response.getShortCode();
        
        // Get statistics without any clicks
        mockMvc.perform(get("/api/stats/" + shortCode))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.shortCode").value(shortCode))
            .andExpect(jsonPath("$.totalClicks").value(0))
            .andExpect(jsonPath("$.createdAt").exists());
    }
}
