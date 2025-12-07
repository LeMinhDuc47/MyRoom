package com.myroom.bookingservice.usecase.impl;

import com.myroom.bookingservice.data.entity.BookingEventProcessed;
import com.myroom.bookingservice.repository.BookingEventProcessedRepository;
import com.myroom.bookingservice.usecase.BookingEventIdempotencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingEventIdempotencyServiceImpl implements BookingEventIdempotencyService {
    
    private final BookingEventProcessedRepository eventProcessedRepository;
    
    @Override
    @Transactional(readOnly = true)
    public boolean isEventProcessed(String eventId, String consumerName) {
        boolean processed = eventProcessedRepository
            .findSuccessfulProcessing(eventId, consumerName)
            .isPresent();
        
        if (processed) {
            log.debug("Event already processed: eventId={}, consumer={}", eventId, consumerName);
        }
        
        return processed;
    }
    
    @Override
    @Transactional
    public BookingEventProcessed recordEventProcessing(String eventId, String consumerName) {
        // Check if already processing to prevent concurrent processing
        var existingProcessing = eventProcessedRepository.findIfProcessing(eventId, consumerName);
        if (existingProcessing.isPresent()) {
            log.warn("Event is already being processed: eventId={}, consumer={}", eventId, consumerName);
            return existingProcessing.get();
        }
        
        BookingEventProcessed record = BookingEventProcessed.builder()
            .id(UUID.randomUUID().toString())
            .eventId(eventId)
            .consumerName(consumerName)
            .status(BookingEventProcessed.ProcessingStatus.PROCESSING)
            .createdAt(Instant.now())
            .build();
        
        eventProcessedRepository.save(record);
        log.debug("Recorded event processing: eventId={}, consumer={}", eventId, consumerName);
        
        return record;
    }
    
    @Override
    @Transactional
    public void markEventProcessingSuccess(String eventId, String consumerName) {
        var record = eventProcessedRepository.findIfProcessing(eventId, consumerName);
        if (record.isPresent()) {
            eventProcessedRepository.updateProcessingStatus(
                record.get().getId(),
                BookingEventProcessed.ProcessingStatus.SUCCESS.name(),
                Instant.now(),
                null
            );
            log.info("Marked event processing as success: eventId={}, consumer={}", eventId, consumerName);
        }
    }
    
    @Override
    @Transactional
    public void markEventProcessingFailed(String eventId, String consumerName, String errorMessage) {
        var record = eventProcessedRepository.findIfProcessing(eventId, consumerName);
        if (record.isPresent()) {
            eventProcessedRepository.updateProcessingStatus(
                record.get().getId(),
                BookingEventProcessed.ProcessingStatus.FAILED.name(),
                Instant.now(),
                errorMessage
            );
            log.error("Marked event processing as failed: eventId={}, consumer={}, error={}", 
                     eventId, consumerName, errorMessage);
        }
    }
}
