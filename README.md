# URL Shortener Service

A production-ready Spring Boot application that converts long URLs into short, manageable codes with comprehensive analytics tracking. The service uses hash-based generation with collision handling, supports multiple database backends, and includes extensive property-based testing.

## Features

- **URL Shortening**: Generate unique 7-character alphanumeric short codes for long URLs
- **Smart Redirects**: HTTP 301 redirects from short codes to original URLs
- **Analytics Tracking**: Real-time click counting and timestamp logging
- **Statistics Dashboard**: View total clicks, first/last access times, and paginated click history
- **Idempotent Operations**: Same URL always returns the same short code
- **Collision Handling**: Automatic retry logic with up to 5 attempts
- **Multi-Database Support**: PostgreSQL (production) and H2 (development/testing)
- **Comprehensive Testing**: 155 tests including property-based tests with jqwik

## Technology Stack

- **Java**: 17
- **Framework**: Spring Boot 3.2.1
- **ORM**: Spring Data JPA with Hibernate
- **Databases**: PostgreSQL 12+ / H2 2.2.224
- **Build Tool**: Maven 3.6+
- **Testing**: JUnit 5, jqwik (Property-Based Testing), Spring Boot Test
- **Hash Algorithm**: MD5 with Base62 encoding

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+ (optional, for production profile)

## Quick Start

### 1. Clone and Build

```bash
git clone <repository-url>
cd acortador-urls
mvn clean install
```

### 2. Run the Application

**Development mode (H2 in-memory database):**
```bash
mvn spring-boot:run
```

The application starts at `http://localhost:8080`

**Production mode (PostgreSQL):**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

### 3. Test the API

**Create a short URL:**
```bash
curl -X POST http://localhost:8080/api/shorten \
  -H "Content-Type: application/json" \
  -d '{"url":"https://example.com/very/long/url/path"}'
```

**Response:**
```json
{
  "shortCode": "abc123X",
  "shortUrl": "http://localhost:8080/abc123X",
  "originalUrl": "https://example.com/very/long/url/path",
  "createdAt": "2026-03-04T21:30:00Z"
}
```

**Access the short URL:**
```bash
curl -L http://localhost:8080/abc123X
```

**Get statistics:**
```bash
curl http://localhost:8080/api/stats/abc123X
```

## API Documentation

### Create Short URL

**Endpoint:** `POST /api/shorten`

**Request:**
```json
{
  "url": "https://example.com/very/long/url/path"
}
```

**Response (201 Created):**
```json
{
  "shortCode": "abc123X",
  "shortUrl": "http://localhost:8080/abc123X",
  "originalUrl": "https://example.com/very/long/url/path",
  "createdAt": "2026-03-04T21:30:00Z"
}
```

**Error Response (400 Bad Request):**
```json
{
  "error": "Invalid URL format",
  "message": "URL must include a valid protocol (http or https)",
  "timestamp": "2026-03-04T21:30:00Z",
  "path": "/api/shorten"
}
```

**Validation Rules:**
- URL must start with `http://` or `https://`
- URL must contain a valid domain name
- Maximum length: 2048 characters
- Cannot be empty or contain only whitespace

### Redirect to Original URL

**Endpoint:** `GET /{shortCode}`

**Response:** HTTP 301 redirect with `Location` header

**Error Response (404 Not Found):**
```json
{
  "error": "Short URL not found",
  "message": "The short code 'xyz789' does not exist",
  "timestamp": "2026-03-04T21:30:00Z",
  "path": "/xyz789"
}
```

### Get URL Statistics

**Endpoint:** `GET /api/stats/{shortCode}`

**Response (200 OK):**
```json
{
  "shortCode": "abc123X",
  "originalUrl": "https://example.com/very/long/url/path",
  "totalClicks": 42,
  "createdAt": "2026-03-04T21:30:00Z",
  "firstAccessAt": "2026-03-04T22:00:00Z",
  "lastAccessAt": "2026-03-05T14:22:00Z"
}
```

### Get Click History

**Endpoint:** `GET /api/stats/{shortCode}/history?page=0&size=20`

**Query Parameters:**
- `page` (optional): Page number, 0-indexed (default: 0)
- `size` (optional): Records per page (default: 20)

**Response (200 OK):**
```json
{
  "shortCode": "abc123X",
  "totalClicks": 42,
  "clicks": [
    {"timestamp": "2026-03-05T14:22:00Z"},
    {"timestamp": "2026-03-05T13:15:00Z"}
  ],
  "page": 0,
  "size": 20,
  "totalPages": 3
}
```

## Configuration

### Application Profiles

The application supports multiple profiles for different environments:

**dev (default)** - Development with H2 in-memory database
```bash
mvn spring-boot:run
```

**postgres** - Production with PostgreSQL
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

**test** - Testing with isolated H2 database (used automatically by tests)

### Database Configuration

#### H2 (Development)

Access H2 Console at `http://localhost:8080/h2-console`

**Connection Settings:**
- JDBC URL: `jdbc:h2:mem:urlshortener`
- Username: `sa`
- Password: (leave empty)

#### PostgreSQL (Production)

1. Create database:
```sql
CREATE DATABASE urlshortener;
```

2. Update `src/main/resources/application-postgres.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/urlshortener
spring.datasource.username=your_username
spring.datasource.password=your_password
```

3. Run with postgres profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

### Environment Variables

Override configuration using environment variables:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/urlshortener
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=secret
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

## Testing

### Run All Tests

```bash
mvn test
```

**Test Coverage:**
- 155 total tests
- Unit tests for all components
- Integration tests for API endpoints
- Property-based tests with jqwik (100+ iterations per property)

### Test Categories

**Unit Tests:**
- URL validation (22 tests)
- Hash generation (4 property tests)
- Service layer logic
- Exception handling

**Integration Tests:**
- API endpoint behavior
- Database persistence
- End-to-end workflows

**Property-Based Tests:**
- Short code format invariants
- URL shortening round trip
- Idempotent operations
- Invalid URL rejection
- Click event recording
- Pagination consistency

### Run Specific Test Classes

```bash
mvn test -Dtest=UrlShortenerControllerTest
mvn test -Dtest=HashGeneratorPropertyTest
```

## Project Structure

```
acortador-urls/
├── src/
│   ├── main/
│   │   ├── java/com/urlshortener/
│   │   │   ├── controller/          # REST API endpoints
│   │   │   │   ├── AnalyticsController.java
│   │   │   │   ├── RedirectController.java
│   │   │   │   └── UrlShortenerController.java
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   │   ├── ClickHistoryPage.java
│   │   │   │   ├── ClickRecord.java
│   │   │   │   ├── ErrorResponse.java
│   │   │   │   ├── RedirectResponse.java
│   │   │   │   ├── ShortenedUrlDto.java
│   │   │   │   ├── ShortenUrlRequest.java
│   │   │   │   └── UrlStatistics.java
│   │   │   ├── entity/              # JPA entities
│   │   │   │   ├── ClickEvent.java
│   │   │   │   └── UrlMapping.java
│   │   │   ├── exception/           # Custom exceptions
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── InvalidUrlException.java
│   │   │   │   ├── ShortCodeGenerationException.java
│   │   │   │   └── ShortUrlNotFoundException.java
│   │   │   ├── hash/                # Hash generation
│   │   │   │   ├── Base62HashGenerator.java
│   │   │   │   └── HashGenerator.java
│   │   │   ├── repository/          # Data access
│   │   │   │   ├── ClickEventRepository.java
│   │   │   │   └── UrlMappingRepository.java
│   │   │   ├── service/             # Business logic
│   │   │   │   ├── AnalyticsService.java
│   │   │   │   ├── AnalyticsServiceImpl.java
│   │   │   │   ├── RedirectService.java
│   │   │   │   ├── RedirectServiceImpl.java
│   │   │   │   ├── URLService.java
│   │   │   │   └── URLServiceImpl.java
│   │   │   ├── validation/          # URL validation
│   │   │   │   ├── URLValidator.java
│   │   │   │   ├── UrlValidatorImpl.java
│   │   │   │   └── ValidationResult.java
│   │   │   └── UrlShortenerApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       ├── application-postgres.properties
│   │       ├── application-test.properties
│   │       ├── schema-h2.sql
│   │       └── schema-postgresql.sql
│   └── test/
│       └── java/com/urlshortener/
│           ├── controller/          # API integration tests
│           ├── dto/                 # DTO serialization tests
│           ├── exception/           # Exception handling tests
│           ├── hash/                # Hash generation property tests
│           ├── service/             # Service layer tests
│           └── validation/          # Validation tests
├── .kiro/specs/acortador-urls/     # Specification documents
│   ├── design.md
│   ├── requirements.md
│   └── tasks.md
├── pom.xml
└── README.md
```

## Architecture

### Layered Architecture

```
┌─────────────────────────────────────┐
│     REST API Layer (Controllers)    │
│  - UrlShortenerController           │
│  - RedirectController               │
│  - AnalyticsController              │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│    Service Layer (Business Logic)   │
│  - URLService                       │
│  - RedirectService                  │
│  - AnalyticsService                 │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│   Repository Layer (Data Access)    │
│  - UrlMappingRepository             │
│  - ClickEventRepository             │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│      Database (PostgreSQL/H2)       │
└─────────────────────────────────────┘
```

### Hash Generation Algorithm

1. **Primary Method**: MD5 hash of URL → Base62 encoding → 7 characters
2. **Collision Resolution**: Generate random Base62 codes
3. **Retry Logic**: Up to 5 attempts before failure

**Base62 Character Set**: `0-9A-Za-z` (62 characters)

### Database Schema

**url_mappings**
```sql
CREATE TABLE url_mappings (
    id BIGSERIAL PRIMARY KEY,
    short_code VARCHAR(10) UNIQUE NOT NULL,
    long_url VARCHAR(2048) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_short_code ON url_mappings(short_code);
```

**click_events**
```sql
CREATE TABLE click_events (
    id BIGSERIAL PRIMARY KEY,
    short_code VARCHAR(10) NOT NULL,
    accessed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (short_code) REFERENCES url_mappings(short_code)
);
CREATE INDEX idx_short_code_accessed ON click_events(short_code, accessed_at DESC);
```

## Error Handling

All errors return consistent JSON responses:

**400 Bad Request** - Invalid input
- Missing or empty URL
- Invalid URL format
- URL exceeds maximum length

**404 Not Found** - Resource not found
- Short code doesn't exist

**500 Internal Server Error** - Server errors
- Hash generation failure
- Database errors
- Collision retry limit exceeded

## Performance Considerations

- **Redirect Speed**: < 100ms under normal load
- **Database Indexes**: Optimized for fast lookups by short code
- **Analytics**: Non-blocking click recording
- **Connection Pool**: HikariCP with configurable pool size

## Development

### Build from Source

```bash
mvn clean package
```

### Run JAR

```bash
java -jar target/url-shortener-0.0.1-SNAPSHOT.jar
```

### Enable Debug Logging

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--logging.level.com.urlshortener=DEBUG"
```

## Troubleshooting

**Issue**: Tests fail with database errors

**Solution**: Ensure H2 dependency is in test scope and schema files exist

**Issue**: PostgreSQL connection refused

**Solution**: Verify PostgreSQL is running and credentials are correct
```bash
psql -U postgres -d urlshortener -c "SELECT 1"
```

**Issue**: Short code collision errors

**Solution**: Check database constraints and retry logic configuration

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## Documentation

- **Requirements**: `.kiro/specs/acortador-urls/requirements.md`
- **Design**: `.kiro/specs/acortador-urls/design.md`
- **Tasks**: `.kiro/specs/acortador-urls/tasks.md`

## License

This project is licensed under the MIT License.

## Support

For issues and questions, please open an issue in the repository.
