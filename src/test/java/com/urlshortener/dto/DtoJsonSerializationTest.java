package com.urlshortener.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for DTO JSON serialization.
 * 
 * Requirements:
 * - 2.4: Accept URLs in JSON format with defined schema
 * - 2.5: Include both short code and full shortened URL
 * - 7.4: Return statistics in JSON format with defined schema
 */
class DtoJsonSerializationTest {
    
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }
    
    @Test
    void shouldSerializeShortenUrlRequest() throws Exception {
        String json = "{\"url\":\"https://example.com/test\"}";
        
        ShortenUrlRequest request = objectMapper.readValue(json, ShortenUrlRequest.class);
        
        assertThat(request.getUrl()).isEqualTo("https://example.com/test");
    }
    
    @Test
    void shouldSerializeShortenedUrlDto() throws Exception {
        ShortenedUrlDto dto = new ShortenedUrlDto(
            "abc123",
            "http://localhost:8080/abc123",
            "https://example.com/test",
            LocalDateTime.of(2024, 1, 15, 10, 30, 0)
        );
        
        String json = objectMapper.writeValueAsString(dto);
        
        assertThat(json).contains("\"shortCode\":\"abc123\"");
        assertThat(json).contains("\"shortUrl\":\"http://localhost:8080/abc123\"");
        assertThat(json).contains("\"originalUrl\":\"https://example.com/test\"");
        assertThat(json).contains("\"createdAt\":\"2024-01-15T10:30:00Z\"");
    }
    
    @Test
    void shouldSerializeUrlStatistics() throws Exception {
        UrlStatistics stats = new UrlStatistics(
            "abc123",
            "https://example.com/test",
            42L,
            LocalDateTime.of(2024, 1, 15, 10, 30, 0),
            LocalDateTime.of(2024, 1, 15, 11, 0, 0),
            LocalDateTime.of(2024, 1, 16, 14, 22, 0)
        );
        
        String json = objectMapper.writeValueAsString(stats);
        
        assertThat(json).contains("\"shortCode\":\"abc123\"");
        assertThat(json).contains("\"totalClicks\":42");
        assertThat(json).contains("\"createdAt\":\"2024-01-15T10:30:00Z\"");
        assertThat(json).contains("\"firstAccessAt\":\"2024-01-15T11:00:00Z\"");
        assertThat(json).contains("\"lastAccessAt\":\"2024-01-16T14:22:00Z\"");
    }
    
    @Test
    void shouldSerializeClickHistoryPage() throws Exception {
        ClickRecord click1 = new ClickRecord(LocalDateTime.of(2024, 1, 16, 14, 22, 0));
        ClickRecord click2 = new ClickRecord(LocalDateTime.of(2024, 1, 16, 13, 15, 0));
        
        ClickHistoryPage page = new ClickHistoryPage(
            "abc123",
            42L,
            List.of(click1, click2),
            0,
            20,
            3
        );
        
        String json = objectMapper.writeValueAsString(page);
        
        assertThat(json).contains("\"shortCode\":\"abc123\"");
        assertThat(json).contains("\"totalClicks\":42");
        assertThat(json).contains("\"page\":0");
        assertThat(json).contains("\"size\":20");
        assertThat(json).contains("\"totalPages\":3");
        assertThat(json).contains("\"timestamp\":\"2024-01-16T14:22:00Z\"");
        assertThat(json).contains("\"timestamp\":\"2024-01-16T13:15:00Z\"");
    }
    
    @Test
    void shouldSerializeErrorResponse() throws Exception {
        ErrorResponse error = new ErrorResponse(
            "Invalid URL format",
            "URL must include a valid protocol (http or https)"
        );
        error.setPath("/api/shorten");
        error.setTimestamp(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
        
        String json = objectMapper.writeValueAsString(error);
        
        assertThat(json).contains("\"error\":\"Invalid URL format\"");
        assertThat(json).contains("\"message\":\"URL must include a valid protocol (http or https)\"");
        assertThat(json).contains("\"timestamp\":\"2024-01-15T10:30:00Z\"");
        assertThat(json).contains("\"path\":\"/api/shorten\"");
    }
}
