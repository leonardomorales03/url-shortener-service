package com.urlshortener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main application class for the URL Shortener Service.
 * 
 * This Spring Boot application provides URL shortening capabilities with analytics tracking.
 * It supports both PostgreSQL (production) and H2 (development/testing) databases.
 */
@SpringBootApplication
@EnableAsync
public class UrlShortenerApplication {

    public static void main(String[] args) {
        SpringApplication.run(UrlShortenerApplication.class, args);
    }
}
