# Implementation Plan: URL Shortener Service

## Overview

This implementation plan breaks down the URL shortener service into incremental coding tasks. The approach follows a bottom-up strategy: starting with core utilities (hash generation, validation), building the data layer, implementing business services, adding the REST API layer, and finally integrating analytics. Each task builds on previous work and includes testing sub-tasks to validate functionality early.

## Tasks

- [x] 1. Set up project structure and dependencies
  - Create Spring Boot project with Maven/Gradle
  - Add dependencies: Spring Web, Spring Data JPA, PostgreSQL driver, H2 database, jqwik for property testing
  - Configure application.properties for both PostgreSQL and H2 profiles
  - Set up database schema initialization scripts
  - _Requirements: 8.1, 8.2_

- [x] 2. Implement URL validation
  - [x] 2.1 Create URLValidator interface and implementation
    - Implement validation for protocol (http/https)
    - Implement validation for domain format
    - Implement validation for URL length (max 2048 characters)
    - Implement validation for invalid characters
    - Return ValidationResult with error messages
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_
  
  - [x]* 2.2 Write property test for URL validation
    - **Property 4: Invalid URL Rejection**
    - **Validates: Requirements 1.4, 4.1, 4.2, 4.3**
  
  - [x]* 2.3 Write unit tests for URL validation edge cases
    - Test maximum length boundary (2048 characters)
    - Test URLs exceeding maximum length
    - Test various invalid formats
    - _Requirements: 4.4, 4.5_

- [x] 3. Implement hash generation
  - [x] 3.1 Create HashGenerator interface and Base62HashGenerator implementation
    - Implement MD5 hash with Base62 encoding
    - Implement random code generation for collision resolution
    - Ensure codes are 7 characters (within 6-10 range)
    - Use only alphanumeric characters (a-z, A-Z, 0-9)
    - _Requirements: 1.1, 1.5, 1.6_
  
  - [x]* 3.2 Write property test for short code format
    - **Property 1: Short Code Generation Invariants**
    - **Validates: Requirements 1.1, 1.5, 1.6**

- [x] 4. Create data models and repositories
  - [x] 4.1 Create UrlMapping JPA entity
    - Define entity with id, shortCode, longUrl, createdAt fields
    - Add unique constraint on shortCode
    - Add index on shortCode for fast lookups
    - _Requirements: 1.2, 8.3_
  
  - [x] 4.2 Create ClickEvent JPA entity
    - Define entity with id, shortCode, accessedAt fields
    - Add foreign key relationship to UrlMapping
    - Add composite index on (shortCode, accessedAt)
    - _Requirements: 5.2, 6.1, 8.4_
  
  - [x] 4.3 Create Spring Data JPA repositories
    - Create UrlMappingRepository with findByShortCode and existsByShortCode methods
    - Create ClickEventRepository with findByShortCodeOrderByAccessedAtDesc method
    - Add pagination support to ClickEventRepository
    - _Requirements: 1.2, 6.2, 7.5_

- [x] 5. Implement URL shortening service
  - [x] 5.1 Create URLService interface and implementation
    - Implement createShortUrl method with validation
    - Implement collision handling with retry logic (max 5 attempts)
    - Implement getLongUrl method for retrieving original URLs
    - Check for existing URL and return existing short code if found
    - _Requirements: 1.1, 1.2, 1.3, 9.1, 9.2_
  
  - [x]* 5.2 Write property test for URL shortening round trip
    - **Property 2: URL Shortening Round Trip**
    - **Validates: Requirements 1.2, 3.1**
  
  - [x]* 5.3 Write property test for idempotent shortening
    - **Property 3: Idempotent URL Shortening**
    - **Validates: Requirements 1.3**
  
  - [x]* 5.4 Write unit tests for collision handling
    - Test collision retry logic
    - Test retry limit exceeded scenario
    - _Requirements: 9.2, 9.3_

- [x] 6. Checkpoint - Ensure core shortening works
  - Ensure all tests pass, ask the user if questions arise.

- [x] 7. Implement redirect service
  - [x] 7.1 Create RedirectService interface and implementation
    - Implement handleRedirect method to lookup URL by short code
    - Return RedirectResponse with target URL and HTTP 301 status
    - Validate stored URL format before redirecting
    - Throw ShortUrlNotFoundException for non-existent codes
    - _Requirements: 3.1, 3.2, 3.3_
  
  - [x]* 7.2 Write property test for non-existent short code handling
    - **Property 7: Non-existent Short Code Handling**
    - **Validates: Requirements 3.2, 7.3**
  
  - [x]* 7.3 Write property test for stored URL validity
    - **Property 8: Stored URL Validity**
    - **Validates: Requirements 3.3**

- [x] 8. Implement analytics service
  - [x] 8.1 Create AnalyticsService interface and implementation
    - Implement recordClick method to create ClickEvent records
    - Implement getStatistics method to calculate total clicks and timestamps
    - Implement getClickHistory method with pagination support
    - Ensure click recording doesn't block redirect operations
    - _Requirements: 5.1, 5.2, 5.5, 6.1, 6.2, 7.1, 7.2, 7.5, 7.6_
  
  - [x]* 8.2 Write property test for click event recording
    - **Property 9: Click Event Recording**
    - **Validates: Requirements 3.4, 5.1, 5.2, 5.5**
  
  - [x]* 8.3 Write property test for chronological logging
    - **Property 10: Chronological Click Logging**
    - **Validates: Requirements 6.1, 6.2, 6.5**
  
  - [x]* 8.4 Write property test for statistics accuracy
    - **Property 11: Statistics Calculation Accuracy**
    - **Validates: Requirements 7.6**
  
  - [x]* 8.5 Write property test for pagination consistency
    - **Property 12: Pagination Consistency**
    - **Validates: Requirements 7.5**
  
  - [x]* 8.6 Write unit tests for analytics edge cases
    - Test statistics for URL with no clicks
    - Test pagination with various page sizes
    - Test first and last access timestamp calculations
    - _Requirements: 7.5, 7.6_

- [x] 9. Integrate redirect service with analytics
  - [x] 9.1 Update RedirectService to call AnalyticsService
    - Call recordClick after successful URL lookup
    - Ensure analytics recording doesn't delay redirect response
    - Handle analytics failures gracefully without affecting redirects
    - _Requirements: 3.4, 5.3_

- [x] 10. Checkpoint - Ensure analytics integration works
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 11. Create REST API controllers
  - [x] 11.1 Create UrlShortenerController
    - Implement POST /api/shorten endpoint
    - Accept ShortenUrlRequest DTO with URL field
    - Return ShortenedUrlDto with 201 status on success
    - Return 400 status with error details on validation failure
    - Include both shortCode and full shortUrl in response
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_
  
  - [x] 11.2 Create RedirectController
    - Implement GET /{shortCode} endpoint
    - Return 301/302 redirect with Location header
    - Return 404 with error message for non-existent codes
    - _Requirements: 3.1, 3.2_
  
  - [x] 11.3 Create AnalyticsController
    - Implement GET /api/stats/{shortCode} endpoint
    - Return UrlStatistics DTO with total clicks and timestamps
    - Implement GET /api/stats/{shortCode}/history endpoint with pagination
    - Return 404 for non-existent short codes
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6_
  
  - [ ]* 11.4 Write property test for successful response format
    - **Property 5: Successful Shortening Response Format**
    - **Validates: Requirements 2.2, 2.5**
  
  - [ ]* 11.5 Write property test for error response format
    - **Property 6: Error Response Format**
    - **Validates: Requirements 2.3**
  
  - [ ]* 11.6 Write unit tests for API endpoints
    - Test POST /api/shorten with valid and invalid inputs
    - Test GET /{shortCode} redirect behavior
    - Test GET /api/stats endpoints
    - Test JSON request/response formats
    - _Requirements: 2.1, 2.4, 7.4_

- [ ] 12. Implement global exception handling
  - [x] 12.1 Create GlobalExceptionHandler with @ControllerAdvice
    - Handle InvalidUrlException → 400 Bad Request
    - Handle ShortUrlNotFoundException → 404 Not Found
    - Handle ShortCodeGenerationException → 500 Internal Server Error
    - Handle generic exceptions → 500 Internal Server Error
    - Return consistent ErrorResponse DTO for all errors
    - _Requirements: 1.4, 3.2, 9.3_

- [ ] 13. Create DTOs for API requests and responses
  - [x] 13.1 Create request/response DTOs
    - Create ShortenUrlRequest DTO
    - Create ShortenedUrlDto response DTO
    - Create UrlStatistics DTO
    - Create ClickHistoryPage DTO with pagination metadata
    - Create ErrorResponse DTO
    - Add JSON annotations for proper serialization
    - _Requirements: 2.4, 2.5, 7.4_

- [ ] 14. Write integration tests
  - [ ]* 14.1 Write end-to-end integration tests
    - Test complete flow: create short URL → redirect → check analytics
    - Test error scenarios across all endpoints
    - Test database persistence and retrieval
    - Test both H2 and PostgreSQL configurations
    - _Requirements: 1.2, 3.1, 5.1, 8.1_

- [ ] 15. Add database migration scripts
  - [x] 15.1 Create SQL schema initialization scripts
    - Create url_mappings table with constraints and indexes
    - Create click_events table with foreign key and indexes
    - Add scripts for both PostgreSQL and H2
    - _Requirements: 8.2, 8.3, 8.4_

- [ ] 16. Configure application properties
  - [x] 16.1 Set up application configuration files
    - Create application.properties with default H2 configuration
    - Create application-postgres.properties for PostgreSQL
    - Configure JPA settings (ddl-auto, show-sql)
    - Configure server port and context path
    - _Requirements: 8.1_

- [ ] 17. Final checkpoint - Run full test suite
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Property tests validate universal correctness properties with 100+ iterations
- Unit tests validate specific examples and edge cases
- Integration tests ensure end-to-end functionality
- The implementation follows a layered architecture: utilities → data → services → API
- Collision handling uses retry logic with exponential fallback to random generation
- Analytics recording is designed to not block redirect operations
