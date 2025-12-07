-- Migration: Create booking_outbox_events table for Transactional Outbox Pattern
-- Purpose: Store booking events that need to be published to Kafka
-- This ensures exactly-once delivery guarantee using polling-based publisher

CREATE TABLE IF NOT EXISTS booking_outbox_events (
    event_id VARCHAR(36) PRIMARY KEY,
    booking_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    topic VARCHAR(100) NOT NULL,
    payload LONGTEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    max_retries INT NOT NULL DEFAULT 5,
    last_error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP NULL,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    
    -- Indexes for efficient querying
    INDEX idx_booking_outbox_status (status),
    INDEX idx_booking_outbox_created_at (created_at),
    INDEX idx_booking_outbox_booking_id (booking_id),
    
    -- Foreign key constraint
    CONSTRAINT fk_booking_outbox_booking_id 
        FOREIGN KEY (booking_id) 
        REFERENCES bookings(id) 
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Migration: Create booking_event_processed table for idempotency
-- Purpose: Track which events have been processed by which consumers
-- Prevents duplicate processing when messages are retried

CREATE TABLE IF NOT EXISTS booking_event_processed (
    id VARCHAR(36) PRIMARY KEY,
    event_id VARCHAR(36) NOT NULL,
    consumer_name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PROCESSING',
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL,
    
    -- Indexes for efficient lookup
    INDEX idx_event_processed_consumer (event_id, consumer_name),
    INDEX idx_event_processed_status (status),
    
    -- Unique constraint to ensure only one processing record per event+consumer
    UNIQUE KEY uk_event_processed_event_consumer (event_id, consumer_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add eventId column to booking_outbox_events if using Kafka message keys
ALTER TABLE booking_outbox_events ADD COLUMN IF NOT EXISTS message_key VARCHAR(36);
