package com.urlshortener.controller;

import com.urlshortener.dto.ErrorResponse;
import com.urlshortener.dto.RedirectResponse;
import com.urlshortener.exception.ShortUrlNotFoundException;
import com.urlshortener.service.RedirectService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for URL redirection operations.
 * 
 * Requirements:
 * - 3.1: Return HTTP 301 redirect with Location header for valid short codes
 * - 3.2: Return HTTP 404 with error message for non-existent codes
 */
@RestController
public class RedirectController {
    
    private final RedirectService redirectService;
    
    @Autowired
    public RedirectController(RedirectService redirectService) {
        this.redirectService = redirectService;
    }
    
    /**
     * Redirects to the original URL for the given short code.
     * 
     * @param shortCode the short code to redirect
     * @return ResponseEntity with 301 redirect and Location header
     */
    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        RedirectResponse redirectResponse = redirectService.handleRedirect(shortCode);
        
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.LOCATION, redirectResponse.getTargetUrl());
        
        return ResponseEntity
            .status(redirectResponse.getStatusCode())
            .headers(headers)
            .build();
    }
    
    /**
     * Exception handler for ShortUrlNotFoundException.
     * Returns 404 Not Found with error details.
     */
    @ExceptionHandler(ShortUrlNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleShortUrlNotFound(
            ShortUrlNotFoundException ex,
            HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
            "Short URL not found",
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
}
