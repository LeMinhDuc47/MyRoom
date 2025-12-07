package com.myroom.bookingservice.usecase.impl;

import com.myroom.bookingservice.data.entity.BookingOutboxEvent;
import com.myroom.bookingservice.repository.BookingOutboxEventRepository;
import com.myroom.bookingservice.usecase.BookingOutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingOutboxServiceImpl implements BookingOutboxService {
    
    private final BookingOutboxEventRepository outboxEventRepository;
    
    @Override
    @Transactional
    public BookingOutboxEvent createOutboxEvent(String bookingId,
                                                String eventType,
                                                String topic,
                                                String payload) {
        BookingOutboxEvent event = BookingOutboxEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .bookingId(bookingId)
            .eventType(eventType)
            .topic(topic)
            .payload(payload)
            .status(BookingOutboxEvent.OutboxEventStatus.PENDING)
            .retryCount(0)
            .maxRetries(5)
            .createdAt(Instant.now())
            .build();
        
        outboxEventRepository.save(event);
        log.debug("Created outbox event: eventId={}, bookingId={}, eventType={}", 
                  event.getEventId(), bookingId, eventType);
        
        return event;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BookingOutboxEvent> findPendingEvents() {
        return outboxEventRepository.findByStatusOrderByCreatedAtAsc(
            BookingOutboxEvent.OutboxEventStatus.PENDING);
    }
    
    @Override
    @Transactional
    public void markEventAsSent(String eventId) {
        outboxEventRepository.markAsSent(eventId, Instant.now(), Instant.now());
        log.info("Marked outbox event as sent: eventId={}", eventId);
    }
    
    @Override
    @Transactional
    public void handleEventRetry(String eventId, String errorMessage) {
        outboxEventRepository.incrementRetry(eventId, errorMessage, Instant.now());
        log.warn("Incremented retry for outbox event: eventId={}, errorMessage={}", eventId, errorMessage);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BookingOutboxEvent> getEventsByBookingId(String bookingId) {
        return outboxEventRepository.findByBookingId(bookingId);
    }
}
