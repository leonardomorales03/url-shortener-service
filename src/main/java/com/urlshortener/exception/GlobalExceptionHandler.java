package com.urlshortener.exception;

import com.urlshortener.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Global exception handler for the URL Shortener application.
 * Provides consistent error responses across all controllers.
 * 
 * Requirements:
 * - 1.4: Reject invalid URL format with descriptive error
 * - 3.2: Return HTTP 404 for non-existent short codes
 * - 9.3: Return error when collision retry limit is exceeded
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * Handles InvalidUrlException and returns 400 Bad Request.
     * 
     * @param ex the InvalidUrlException
     * @param request the HTTP request
     * @return ResponseEntity with ErrorResponse and 400 status
     */
    @ExceptionHandler(InvalidUrlException.class)
    public ResponseEntity<ErrorResponse> handleInvalidUrlException(
            InvalidUrlException ex, 
            HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
            "Invalid URL",
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse);
    }
    
    /**
     * Handles ShortUrlNotFoundException and returns 404 Not Found.
     * 
     * @param ex the ShortUrlNotFoundException
     * @param request the HTTP request
     * @return ResponseEntity with ErrorResponse and 404 status
     */
    @ExceptionHandler(ShortUrlNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleShortUrlNotFoundException(
            ShortUrlNotFoundException ex, 
            HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
            "Short URL not found",
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(errorResponse);
    }
    
    /**
     * Handles ShortCodeGenerationException and returns 500 Internal Server Error.
     * 
     * @param ex the ShortCodeGenerationException
     * @param request the HTTP request
     * @return ResponseEntity with ErrorResponse and 500 status
     */
    @ExceptionHandler(ShortCodeGenerationException.class)
    public ResponseEntity<ErrorResponse> handleShortCodeGenerationException(
            ShortCodeGenerationException ex, 
            HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
            "Code generation failed",
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(errorResponse);
    }
    
    /**
     * Handles all other exceptions and returns 500 Internal Server Error.
     * Returns a safe error message without exposing internal details.
     * 
     * @param ex the Exception
     * @param request the HTTP request
     * @return ResponseEntity with ErrorResponse and 500 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, 
            HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
            "Internal server error",
            "An unexpected error occurred. Please try again later.",
            request.getRequestURI()
        );
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(errorResponse);
    }
}
