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
 * Integration tests for RedirectController.
 * 
 * Requirements:
 * - 3.1: Return HTTP 301 redirect with Location header for valid short codes
 * - 3.2: Return HTTP 404 with error message for non-existent codes
 */
@SpringBootTest
@AutoConfigureMockMvc
class RedirectControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void shouldRedirectToOriginalUrlWith301Status() throws Exception {
        // First, create a short URL
        String longUrl = "https://example.com/redirect-test";
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
        
        // Test redirect
        mockMvc.perform(get("/" + shortCode))
            .andExpect(status().isMovedPermanently())
            .andExpect(header().string("Location", longUrl));
    }
    
    @Test
    void shouldReturn404ForNonExistentShortCode() throws Exception {
        String nonExistentCode = "invalid";
        
        mockMvc.perform(get("/" + nonExistentCode))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Short URL not found"))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/" + nonExistentCode));
    }
    
    @Test
    void shouldRedirectMultipleTimes() throws Exception {
        // Create a short URL
        String longUrl = "https://example.com/multiple-redirects";
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
        
        // Test multiple redirects
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get("/" + shortCode))
                .andExpect(status().isMovedPermanently())
                .andExpect(header().string("Location", longUrl));
        }
    }
}
