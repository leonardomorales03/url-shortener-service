package com.urlshortener.exception;

import com.urlshortener.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for GlobalExceptionHandler.
 * 
 * Requirements:
 * - 1.4: Reject invalid URL format with descriptive error
 * - 3.2: Return HTTP 404 for non-existent short codes
 * - 9.3: Return error when collision retry limit is exceeded
 */
class GlobalExceptionHandlerTest {
    
    private GlobalExceptionHandler exceptionHandler;
    private HttpServletRequest request;
    
    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest("POST", "/api/shorten");
    }
    
    @Test
    void shouldHandleInvalidUrlExceptionWith400() {
        InvalidUrlException exception = new InvalidUrlException("URL must include a valid protocol");
        
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidUrlException(exception, request);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Invalid URL");
        assertThat(response.getBody().getMessage()).isEqualTo("URL must include a valid protocol");
        assertThat(response.getBody().getPath()).isEqualTo("/api/shorten");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }
    
    @Test
    void shouldHandleShortUrlNotFoundExceptionWith404() {
        ShortUrlNotFoundException exception = new ShortUrlNotFoundException("xyz789", "does not exist");
        MockHttpServletRequest getRequest = new MockHttpServletRequest("GET", "/xyz789");
        
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleShortUrlNotFoundException(exception, getRequest);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Short URL not found");
        assertThat(response.getBody().getMessage()).contains("xyz789");
        assertThat(response.getBody().getPath()).isEqualTo("/xyz789");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }
    
    @Test
    void shouldHandleShortCodeGenerationExceptionWith500() {
        ShortCodeGenerationException exception = new ShortCodeGenerationException(
            "Unable to generate unique short code after 5 attempts"
        );
        
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleShortCodeGenerationException(exception, request);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Code generation failed");
        assertThat(response.getBody().getMessage()).contains("Unable to generate unique short code");
        assertThat(response.getBody().getPath()).isEqualTo("/api/shorten");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }
    
    @Test
    void shouldHandleGenericExceptionWith500() {
        Exception exception = new RuntimeException("Unexpected database error");
        
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception, request);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Internal server error");
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred. Please try again later.");
        assertThat(response.getBody().getPath()).isEqualTo("/api/shorten");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }
    
    @Test
    void shouldNotExposeInternalDetailsInGenericException() {
        Exception exception = new RuntimeException("Database connection failed: password=secret123");
        
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception, request);
        
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).doesNotContain("password");
        assertThat(response.getBody().getMessage()).doesNotContain("secret123");
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred. Please try again later.");
    }
}
