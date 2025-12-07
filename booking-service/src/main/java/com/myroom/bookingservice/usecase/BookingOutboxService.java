package com.myroom.bookingservice.usecase;

import com.myroom.bookingservice.data.entity.BookingOutboxEvent;
import java.util.List;

public interface BookingOutboxService {
    
    /**
     * Create and save an outbox event (must be called within same transaction as domain change)
     */
    BookingOutboxEvent createOutboxEvent(String bookingId, 
                                         String eventType, 
                                         String topic, 
                                         String payload);
    
    /**
     * Find all pending outbox events ready to publish
     */
    List<BookingOutboxEvent> findPendingEvents();
    
    /**
     * Mark outbox event as successfully sent
     */
    void markEventAsSent(String eventId);
    
    /**
     * Handle failed event with retry logic
     */
    void handleEventRetry(String eventId, String errorMessage);
    
    /**
     * Get events by booking ID for tracking
     */
    List<BookingOutboxEvent> getEventsByBookingId(String bookingId);
}
