package com.myroom.bookingservice.usecase;

import com.myroom.bookingservice.data.entity.BookingEventProcessed;

public interface BookingEventIdempotencyService {
    
    /**
     * Check if an event has already been successfully processed by a consumer
     */
    boolean isEventProcessed(String eventId, String consumerName);
    
    /**
     * Record that an event is being processed (lock to prevent concurrent processing)
     */
    BookingEventProcessed recordEventProcessing(String eventId, String consumerName);
    
    /**
     * Mark event processing as completed successfully
     */
    void markEventProcessingSuccess(String eventId, String consumerName);
    
    /**
     * Mark event processing as failed
     */
    void markEventProcessingFailed(String eventId, String consumerName, String errorMessage);
}
