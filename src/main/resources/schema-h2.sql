-- H2 Database Schema for URL Shortener

-- Drop tables if they exist (for clean initialization)
DROP TABLE IF EXISTS click_events;
DROP TABLE IF EXISTS url_mappings;

-- URL Mappings Table
CREATE TABLE url_mappings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    short_code VARCHAR(10) UNIQUE NOT NULL,
    long_url VARCHAR(2048) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for fast lookups by short code
CREATE INDEX idx_short_code ON url_mappings(short_code);

-- Click Events Table
CREATE TABLE click_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    short_code VARCHAR(10) NOT NULL,
    accessed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (short_code) REFERENCES url_mappings(short_code) ON DELETE CASCADE
);

-- Composite index for efficient queries on short_code and accessed_at
CREATE INDEX idx_short_code_accessed ON click_events(short_code, accessed_at DESC);
