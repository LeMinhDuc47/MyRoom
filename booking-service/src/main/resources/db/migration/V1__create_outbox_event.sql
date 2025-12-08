-- Migration V1: Create outbox_event table
CREATE TABLE IF NOT EXISTS outbox_event (
    id VARCHAR(36) PRIMARY KEY NOT NULL COMMENT 'Unique event ID (UUID)',
    aggregate_type VARCHAR(255) NOT NULL COMMENT 'Type of aggregate (e.g., Booking)',
    aggregate_id VARCHAR(255) NOT NULL COMMENT 'ID of the aggregate',
    type VARCHAR(255) NOT NULL COMMENT 'Event type (e.g., BOOKING_CREATED)',
    payload LONGTEXT NOT NULL COMMENT 'Event payload as JSON',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Event creation timestamp',
    processed BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Whether event has been processed',
    processed_at TIMESTAMP NULL COMMENT 'Timestamp when event was processed',
    INDEX idx_processed (processed),
    INDEX idx_created_at (created_at),
    INDEX idx_aggregate_id (aggregate_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Outbox table for transactional outbox pattern';
