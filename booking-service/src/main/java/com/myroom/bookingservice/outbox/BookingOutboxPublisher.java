package com.myroom.bookingservice.outbox;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Background job that publishes pending outbox events to Kafka
 * This is the core of the Transactional Outbox Pattern
 *
 * How it works:
 * 1. Polls booking_outbox_events table every 1 second
 * 2. Finds all events with processed=false (PENDING)
 * 3. Publishes each event to Kafka
 * 4. Marks event as processed (processed=true) when successfully published
 * 5. On Kafka failure: Event stays in DB, will retry next cycle
 *
 * Benefits:
 * - No message loss even if Kafka is down
 * - Automatic retry without manual intervention
 * - Guaranteed at-least-once delivery
 */
@Component
@Slf4j
public class BookingOutboxPublisher {
    
    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public BookingOutboxPublisher(OutboxEventRepository outboxEventRepository,
                                  KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Poll for pending outbox events and publish to Kafka
     * Runs every 1 second (fixedDelay = 1000ms)
     * 
     * This scheduler automatically runs in background without any manual trigger
     * 
     * PATTERN DISABLED: @Scheduled commented out
     * To ENABLE pattern: Uncomment @Scheduled below
     * To DISABLE pattern: Keep commented (background job won't run)
     */
    // @Scheduled(fixedDelay = 1000)  // Every 1 second
    // @Transactional
    public void publishPendingEvents() {
        try {
            // Find all PENDING events (processed = false), ordered by creation time
            List<OutboxEvent> pendingEvents = outboxEventRepository.findByProcessedFalseOrderByCreatedAtAsc();
            
            if (pendingEvents.isEmpty()) {
                return;
            }
            
            log.info("Found {} pending outbox events to publish", pendingEvents.size());
            
            // Try to publish each event
            for (OutboxEvent event : pendingEvents) {
                try {
                    publishEventToKafka(event);
                } catch (Exception e) {
                    log.error("Failed to publish outbox event {}: {}", event.getId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error in publishPendingEvents: {}", e.getMessage(), e);
        }
    }

    /**
     * Publish single outbox event to Kafka
     * 
     * Send message to Kafka topic
     * On success: Mark event as processed (processed=true)
     * On failure: Leave as is (processed=false), will retry next cycle
     */
    private void publishEventToKafka(OutboxEvent event) {
        try {
            log.debug("Publishing outbox event {} to Kafka topic: {}", event.getId(), event.getType());
            
            // Build Kafka message with aggregateId as key (for ordering)
            Message<String> message = MessageBuilder
                    .withPayload(event.getPayload())
                    .setHeader(KafkaHeaders.TOPIC, event.getType())
                    .setHeader("kafka_messageKey", event.getAggregateId())
                    .setHeader("eventId", event.getId())
                    .setHeader("eventType", event.getType())
                    .setHeader("aggregateId", event.getAggregateId())
                    .build();
            
            // Send to Kafka (synchronous to ensure it succeeds before marking as processed)
            kafkaTemplate.send(message).get();  // Blocking call
            
            log.info("Successfully published outbox event {} to Kafka topic {}", event.getId(), event.getType());
            
            // Mark as processed once successfully published to Kafka
            event.setProcessed(true);
            event.setProcessedAt(Instant.now());
            outboxEventRepository.save(event);
            
            log.info("Marked outbox event {} as processed", event.getId());
            
        } catch (Exception e) {
            log.error("Failed to send outbox event {} to Kafka: {}", event.getId(), e.getMessage());
            throw new RuntimeException("Failed to publish event to Kafka: " + e.getMessage(), e);
        }
    }
}
