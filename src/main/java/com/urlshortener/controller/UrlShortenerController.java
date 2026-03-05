package com.urlshortener.controller;

import com.urlshortener.dto.ErrorResponse;
import com.urlshortener.dto.ShortenUrlRequest;
import com.urlshortener.dto.ShortenedUrlDto;
import com.urlshortener.exception.InvalidUrlException;
import com.urlshortener.service.URLService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * REST controller for URL shortening operations.
 * 
 * Requirements:
 * - 2.1: Expose POST endpoint that accepts long URL in request body
 * - 2.2: Return short code with HTTP status 201 on success
 * - 2.3: Return appropriate HTTP error status with error details on failure
 * - 2.4: Accept URLs in JSON format with defined schema
 * - 2.5: Include both short code and full shortened URL in response
 */
@RestController
@RequestMapping("/api")
public class UrlShortenerController {
    
    private final URLService urlService;
    
    @Autowired
    public UrlShortenerController(URLService urlService) {
        this.urlService = urlService;
    }
    
    /**
     * Creates a short URL for the provided long URL.
     * 
     * @param request the URL shortening request containing the long URL
     * @return ResponseEntity with ShortenedUrlDto and 201 status on success
     */
    @PostMapping("/shorten")
    public ResponseEntity<ShortenedUrlDto> shortenUrl(
            @Valid @RequestBody ShortenUrlRequest request) {
        ShortenedUrlDto result = urlService.createShortUrl(request.getUrl());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
    
    /**
     * Exception handler for InvalidUrlException.
     * Returns 400 Bad Request with error details.
     */
    @ExceptionHandler(InvalidUrlException.class)
    public ResponseEntity<ErrorResponse> handleInvalidUrl(
            InvalidUrlException ex, 
            HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
            "Invalid URL format",
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * Exception handler for validation errors.
     * Returns 400 Bad Request with validation error details.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        StringBuilder message = new StringBuilder();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            if (message.length() > 0) {
                message.append(", ");
            }
            message.append(error.getDefaultMessage());
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
            "Validation failed",
            message.toString(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
