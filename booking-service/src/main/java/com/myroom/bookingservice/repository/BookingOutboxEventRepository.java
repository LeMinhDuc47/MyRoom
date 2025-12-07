package com.myroom.bookingservice.repository;

import com.myroom.bookingservice.data.entity.BookingOutboxEvent;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface BookingOutboxEventRepository extends JpaRepository<BookingOutboxEvent, String> {
    
    // Find all pending events to publish
    List<BookingOutboxEvent> findByStatusOrderByCreatedAtAsc(BookingOutboxEvent.OutboxEventStatus status);
    
    //Find events by booking ID
    List<BookingOutboxEvent> findByBookingId(String bookingId);
    
    // Find events that failed and need retry
    @Query("SELECT boe FROM BookingOutboxEvent boe " +
           "WHERE boe.status = com.myroom.bookingservice.data.entity.BookingOutboxEvent.OutboxEventStatus.PENDING " +
           "AND boe.retryCount < boe.maxRetries " +
           "ORDER BY boe.createdAt ASC")
    List<BookingOutboxEvent> findRetryableEvents();
    
    
    // Update status and sent time after successful publishing
    @Modifying
    @Query("UPDATE BookingOutboxEvent boe " +
           "SET boe.status = 'SENT', boe.sentAt = :sentAt, boe.updatedAt = :updatedAt " +
           "WHERE boe.eventId = :eventId")
    void markAsSent(@Param("eventId") String eventId, 
                    @Param("sentAt") Instant sentAt,
                    @Param("updatedAt") Instant updatedAt);
    
    
    // Update retry count and error message
    @Modifying
    @Query("UPDATE BookingOutboxEvent boe " +
           "SET boe.retryCount = boe.retryCount + 1, " +
           "    boe.lastErrorMessage = :errorMessage, " +
           "    boe.updatedAt = :updatedAt, " +
           "    boe.status = CASE WHEN boe.retryCount + 1 >= boe.maxRetries THEN 'FAILED' ELSE 'PENDING' END " +
           "WHERE boe.eventId = :eventId")
    void incrementRetry(@Param("eventId") String eventId,
                       @Param("errorMessage") String errorMessage,
                       @Param("updatedAt") Instant updatedAt);
    
    // Find old sent events that can be cleaned up (older than 7 days)
    @Query("SELECT boe FROM BookingOutboxEvent boe " +
           "WHERE boe.status = 'SENT' " +
           "AND boe.sentAt < :cutoffTime")
    List<BookingOutboxEvent> findOldSentEvents(@Param("cutoffTime") Instant cutoffTime);
    
    // Delete old sent events
    @Modifying
    @Query("DELETE FROM BookingOutboxEvent boe " +
           "WHERE boe.status = 'SENT' " +
           "AND boe.sentAt < :cutoffTime")
    int deleteOldSentEvents(@Param("cutoffTime") Instant cutoffTime);
}
