package com.myroom.bookingservice.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myroom.bookingservice.data.entity.BookingDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class OutboxService {
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public OutboxService(OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public OutboxEvent saveBookingCreatedEvent(BookingDetails bookingDetails) {
        try {
            String payload = objectMapper.writeValueAsString(bookingDetails);
            OutboxEvent event = new OutboxEvent(
                    "Booking",
                    bookingDetails.getId(),
                    "BOOKING_CREATED",
                    payload
            );
            OutboxEvent savedEvent = outboxEventRepository.save(event);
            log.info("Outbox event created for booking: {}", bookingDetails.getId());
            return savedEvent;
        } catch (Exception e) {
            log.error("Failed to create outbox event for booking {}: {}", bookingDetails.getId(), e.getMessage());
            throw new RuntimeException("Failed to save outbox event", e);
        }
    }

    @Transactional
    public void markAsProcessed(String eventId) {
        try {
            OutboxEvent event = outboxEventRepository.findById(eventId).orElse(null);
            if (event != null) {
                event.setProcessed(true);
                event.setProcessedAt(java.time.Instant.now());
                outboxEventRepository.save(event);
                log.debug("Marked outbox event {} as processed", eventId);
            }
        } catch (Exception e) {
            log.error("Failed to mark outbox event {} as processed: {}", eventId, e.getMessage());
        }
    }
}
