package com.myroom.bookingservice.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "booking_event_processed", indexes = {
    @Index(name = "idx_event_processed_consumer", columnList = "event_id, consumer_name"),
    @Index(name = "idx_event_processed_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingEventProcessed {
    
    @Id
    @Column(name = "id")
    private String id;
    
    @Column(name = "event_id", nullable = false)
    private String eventId;
    
    @Column(name = "consumer_name", nullable = false)
    private String consumerName; 
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProcessingStatus status;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "processed_at")
    private Instant processedAt;
    
    @PrePersist
    protected void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }
    
    public enum ProcessingStatus {
        PROCESSING, SUCCESS, FAILED
    }
}
