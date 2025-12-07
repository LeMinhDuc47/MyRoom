package com.myroom.bookingservice.listener;

import com.myroom.bookingservice.usecase.BookingEventIdempotencyService;
import com.myroom.bookingservice.usecase.BookingService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class BookingPaymentEventListener {
    private static final String CONSUMER_NAME = "BookingPaymentEventListener";
    
    @Autowired
    BookingService bookingService;
    
    @Autowired
    BookingEventIdempotencyService idempotencyService;

    @RetryableTopic(attempts = "4", backoff = @Backoff(delay = 2000, multiplier = 2.0), autoCreateTopics = "true", dltTopicSuffix = "-dlt", exclude = {
            JSONException.class })
    @KafkaListener(topics = "booking.payment")
    @Transactional
    public void listen(@Payload final String payload) {
        log.info("Received booking payment event (kafka topic: 'booking.payment') with payload:{}", payload);
        String eventId = null;
        try {
            JSONObject jsonPayload = new JSONObject(payload);
            eventId = jsonPayload.optString("eventId", null); // Extract eventId from payload
            String bookingId = jsonPayload.getString("bookingId");
            String status = jsonPayload.getString("status");
            
            // Check idempotency: if this event has been successfully processed before, skip
            if (eventId != null && idempotencyService.isEventProcessed(eventId, CONSUMER_NAME)) {
                log.warn("Event already processed by consumer: eventId={}, consumer={}", eventId, CONSUMER_NAME);
                return;
            }
            
            // Record that we're processing this event (lock to prevent concurrent processing)
            if (eventId != null) {
                idempotencyService.recordEventProcessing(eventId, CONSUMER_NAME);
            }
            
            if ("complete".equals(status)) {
                bookingService.updateBookingStatusAndBookingPaymentMetaDataModel(bookingId, status);
            } else {
                // treat any non-complete payment status as cancel/compensation
                bookingService.handleBookingPaymentCancel(bookingId);
            }
            
            // Mark event processing as successful
            if (eventId != null) {
                idempotencyService.markEventProcessingSuccess(eventId, CONSUMER_NAME);
            }
            log.info("Successfully processed event: eventId={}, bookingId={}", eventId, bookingId);
            
        } catch (JSONException jsonException) {
            log.error("Invalid message received: {}", jsonException.getMessage());
            if (eventId != null) {
                idempotencyService.markEventProcessingFailed(eventId, CONSUMER_NAME, jsonException.getMessage());
            }
            throw jsonException;
        } catch (Exception ex) {
            log.error("Some error occurred: {}", ex.getMessage());
            if (eventId != null) {
                idempotencyService.markEventProcessingFailed(eventId, CONSUMER_NAME, ex.getMessage());
            }
            throw ex;
        }
    }
}