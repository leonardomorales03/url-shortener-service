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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for UrlShortenerController.
 * 
 * Requirements:
 * - 2.1: POST endpoint accepts long URL in request body
 * - 2.2: Return short code with HTTP status 201 on success
 * - 2.3: Return 400 status with error details on validation failure
 * - 2.4: Accept URLs in JSON format
 * - 2.5: Include both shortCode and full shortUrl in response
 */
@SpringBootTest
@AutoConfigureMockMvc
class UrlShortenerControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void shouldCreateShortUrlAndReturn201() throws Exception {
        ShortenUrlRequest request = new ShortenUrlRequest("https://example.com/very/long/url/path");
        
        MvcResult result = mockMvc.perform(post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.shortCode").exists())
            .andExpect(jsonPath("$.shortUrl").exists())
            .andExpect(jsonPath("$.originalUrl").value("https://example.com/very/long/url/path"))
            .andExpect(jsonPath("$.createdAt").exists())
            .andReturn();
        
        String responseBody = result.getResponse().getContentAsString();
        ShortenedUrlDto response = objectMapper.readValue(responseBody, ShortenedUrlDto.class);
        
        assertThat(response.getShortCode()).isNotEmpty();
        assertThat(response.getShortCode().length()).isBetween(6, 10);
        assertThat(response.getShortUrl()).contains(response.getShortCode());
    }
    
    @Test
    void shouldReturn400ForInvalidUrlFormat() throws Exception {
        ShortenUrlRequest request = new ShortenUrlRequest("not-a-valid-url");
        
        mockMvc.perform(post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").exists())
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.timestamp").exists());
    }
    
    @Test
    void shouldReturn400ForEmptyUrl() throws Exception {
        ShortenUrlRequest request = new ShortenUrlRequest("");
        
        mockMvc.perform(post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Validation failed"))
            .andExpect(jsonPath("$.message").value("URL cannot be empty"));
    }
    
    @Test
    void shouldReturn400ForUrlWithoutProtocol() throws Exception {
        ShortenUrlRequest request = new ShortenUrlRequest("example.com/path");
        
        mockMvc.perform(post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Invalid URL format"))
            .andExpect(jsonPath("$.message").exists());
    }
    
    @Test
    void shouldReturnSameShortCodeForDuplicateUrl() throws Exception {
        String url = "https://example.com/duplicate-test";
        ShortenUrlRequest request = new ShortenUrlRequest(url);
        
        // First request
        MvcResult result1 = mockMvc.perform(post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();
        
        ShortenedUrlDto response1 = objectMapper.readValue(
            result1.getResponse().getContentAsString(), 
            ShortenedUrlDto.class
        );
        
        // Second request with same URL
        MvcResult result2 = mockMvc.perform(post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();
        
        ShortenedUrlDto response2 = objectMapper.readValue(
            result2.getResponse().getContentAsString(), 
            ShortenedUrlDto.class
        );
        
        // Should return the same short code (idempotent)
        assertThat(response1.getShortCode()).isEqualTo(response2.getShortCode());
    }
}
