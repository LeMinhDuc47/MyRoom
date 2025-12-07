package com.myroom.bookingservice.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "booking_outbox_events", indexes = {
    @Index(name = "idx_booking_outbox_status", columnList = "status"),
    @Index(name = "idx_booking_outbox_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingOutboxEvent {
    
    @Id
    @Column(name = "event_id")
    private String eventId;
    
    @Column(name = "booking_id", nullable = false)
    private String bookingId;
    
    @Column(name = "event_type", nullable = false)
    private String eventType;
    
    @Column(name = "topic", nullable = false)
    private String topic; 
    
    @Column(name = "payload", nullable = false, columnDefinition = "LONGTEXT")
    private String payload; 
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OutboxEventStatus status; 
    
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;
    
    @Column(name = "max_retries", nullable = false)
    private Integer maxRetries;
    
    @Column(name = "last_error_message", columnDefinition = "TEXT")
    private String lastErrorMessage;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "sent_at")
    private Instant sentAt;
    
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @PrePersist
    protected void prePersist() {
        if (this.eventId == null) {
            this.eventId = UUID.randomUUID().toString();
        }
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.retryCount == null) {
            this.retryCount = 0;
        }
        if (this.maxRetries == null) {
            this.maxRetries = 5;
        }
        if (this.status == null) {
            this.status = OutboxEventStatus.PENDING;
        }
    }
    
    @PreUpdate
    protected void preUpdate() {
        this.updatedAt = Instant.now();
    }
    
    public enum OutboxEventStatus {
        PENDING, SENT, FAILED
    }
}
