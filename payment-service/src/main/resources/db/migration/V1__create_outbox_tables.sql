-- Migration: Create payment_outbox_events table for Transactional Outbox Pattern
-- Purpose: Store payment events that need to be published to Kafka
-- This ensures at-least-once delivery guarantee

CREATE TABLE IF NOT EXISTS payment_outbox_events (
    event_id VARCHAR(36) PRIMARY KEY,
    payment_id VARCHAR(255) NOT NULL,
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
    INDEX idx_payment_outbox_status (status),
    INDEX idx_payment_outbox_created_at (created_at),
    INDEX idx_payment_outbox_payment_id (payment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Migration: Create payment_event_processed table for idempotency
-- Purpose: Track which events have been processed by which consumers

CREATE TABLE IF NOT EXISTS payment_event_processed (
    id VARCHAR(36) PRIMARY KEY,
    event_id VARCHAR(36) NOT NULL,
    consumer_name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PROCESSING',
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL,
    
    -- Indexes for efficient lookup
    INDEX idx_payment_event_processed_consumer (event_id, consumer_name),
    INDEX idx_payment_event_processed_status (status),
    
    -- Unique constraint to ensure only one processing record per event+consumer
    UNIQUE KEY uk_payment_event_processed_event_consumer (event_id, consumer_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
