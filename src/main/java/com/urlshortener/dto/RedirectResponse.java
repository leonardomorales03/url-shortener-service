package com.urlshortener.dto;

/**
 * Response object for redirect operations.
 * Contains the target URL and HTTP status code for the redirect.
 */
public class RedirectResponse {
    private final String targetUrl;
    private final int statusCode;

    public RedirectResponse(String targetUrl, int statusCode) {
        this.targetUrl = targetUrl;
        this.statusCode = statusCode;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
