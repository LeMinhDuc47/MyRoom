package com.myroom.bookingservice.usecase.impl;

import com.myroom.bookingservice.data.entity.BookingOutboxEvent;
import com.myroom.bookingservice.usecase.BookingOutboxService;
import com.myroom.bookingservice.usecase.KafkaMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingOutboxPublisher {
    
    private final BookingOutboxService outboxService;
    private final KafkaMessageService kafkaMessageService;
    
    /**
     * Polls outbox table every 1 second to find and publish pending events
     * This ensures at-least-once delivery guarantee:
     * - Event is created in DB within same transaction as domain change
     * - If Kafka send fails, event remains PENDING and will be retried
     * - Once Kafka send succeeds, event is marked SENT
     * - Events marked SENT are not retried
     */
    @Scheduled(fixedDelay = 1000, initialDelay = 2000)
    @Transactional
    public void publishPendingEvents() {
        try {
            List<BookingOutboxEvent> pendingEvents = outboxService.findPendingEvents();
            
            if (!pendingEvents.isEmpty()) {
                log.debug("Found {} pending outbox events to publish", pendingEvents.size());
            }
            
            for (BookingOutboxEvent event : pendingEvents) {
                publishEvent(event);
            }
        } catch (Exception e) {
            log.error("Error in booking outbox publisher scheduled task", e);
        }
    }
    
    private void publishEvent(BookingOutboxEvent event) {
        try {
            log.debug("Publishing outbox event: eventId={}, type={}, topic={}", 
                     event.getEventId(), event.getEventType(), event.getTopic());
            
            // Send to Kafka
            kafkaMessageService.sendMessage(event.getTopic(), event.getPayload());
            
            // Mark as sent only if Kafka send succeeded
            outboxService.markEventAsSent(event.getEventId());
            log.info("Successfully published outbox event: eventId={}, type={}, topic={}", 
                    event.getEventId(), event.getEventType(), event.getTopic());
            
        } catch (Exception e) {
            log.error("Failed to publish outbox event: eventId={}, error={}", 
                     event.getEventId(), e.getMessage(), e);
            
            // Increment retry count and keep status PENDING (or FAILED if max retries exceeded)
            outboxService.handleEventRetry(event.getEventId(), e.getMessage());
        }
    }
}
