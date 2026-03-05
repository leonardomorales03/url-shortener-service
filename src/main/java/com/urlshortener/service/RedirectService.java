package com.urlshortener.service;

import com.urlshortener.dto.RedirectResponse;

/**
 * Service interface for handling URL redirects.
 * 
 * Requirements:
 * - 3.1: Return HTTP 301 redirect to the corresponding long URL
 * - 3.2: Return HTTP 404 for non-existent short codes
 * - 3.3: Validate stored URL format before redirecting
 */
public interface RedirectService {
    /**
     * Handles redirect logic for a short code.
     * Looks up the original URL, validates it, and returns a redirect response.
     * 
     * @param shortCode the short code to redirect
     * @return RedirectResponse containing the target URL and HTTP 301 status
     * @throws com.urlshortener.exception.ShortUrlNotFoundException if short code doesn't exist
     * @throws com.urlshortener.exception.InvalidUrlException if stored URL is invalid
     */
    RedirectResponse handleRedirect(String shortCode);
}
