package com.urlshortener.repository;

import com.urlshortener.model.ClickEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for ClickEvent entities.
 * 
 * Requirements:
 * - 6.2: Maintain chronological log of access events
 * - 7.5: Support pagination for click history
 */
@Repository
public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {
    
    /**
     * Find all click events for a short code, ordered by accessed time descending.
     * 
     * @param shortCode the short code to search for
     * @return List of ClickEvents ordered by most recent first
     */
    List<ClickEvent> findByShortCodeOrderByAccessedAtDesc(String shortCode);
    
    /**
     * Find click events for a short code with pagination support.
     * 
     * @param shortCode the short code to search for
     * @param pageable pagination information
     * @return Page of ClickEvents
     */
    Page<ClickEvent> findByShortCode(String shortCode, Pageable pageable);
    
    /**
     * Count the total number of clicks for a short code.
     * 
     * @param shortCode the short code to count clicks for
     * @return total number of clicks
     */
    long countByShortCode(String shortCode);
    
    /**
     * Find the first (earliest) click event for a short code.
     * 
     * @param shortCode the short code to search for
     * @return Optional containing the first ClickEvent if found
     */
    Optional<ClickEvent> findFirstByShortCodeOrderByAccessedAtAsc(String shortCode);
    
    /**
     * Find the last (most recent) click event for a short code.
     * 
     * @param shortCode the short code to search for
     * @return Optional containing the last ClickEvent if found
     */
    Optional<ClickEvent> findFirstByShortCodeOrderByAccessedAtDesc(String shortCode);
    
    /**
     * Find click events within a date range for a short code.
     * 
     * @param shortCode the short code to search for
     * @param startDate start of the date range
     * @param endDate end of the date range
     * @param pageable pagination information
     * @return Page of ClickEvents within the date range
     */
    @Query("SELECT c FROM ClickEvent c WHERE c.shortCode = :shortCode " +
           "AND c.accessedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY c.accessedAt DESC")
    Page<ClickEvent> findByShortCodeAndDateRange(
        @Param("shortCode") String shortCode,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
}
