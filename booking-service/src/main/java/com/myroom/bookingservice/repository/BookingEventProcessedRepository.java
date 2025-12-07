package com.myroom.bookingservice.repository;

import com.myroom.bookingservice.data.entity.BookingEventProcessed;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
@Transactional
public interface BookingEventProcessedRepository extends JpaRepository<BookingEventProcessed, String> {
    
    @Query("SELECT bep FROM BookingEventProcessed bep " +
           "WHERE bep.eventId = :eventId " +
           "AND bep.consumerName = :consumerName " +
           "AND bep.status = 'SUCCESS'")
    Optional<BookingEventProcessed> findSuccessfulProcessing(@Param("eventId") String eventId,
                                                             @Param("consumerName") String consumerName);
    
    
    //Find if event is being processed (idempotency - prevent concurrent processing)
    @Query("SELECT bep FROM BookingEventProcessed bep " +
           "WHERE bep.eventId = :eventId " +
           "AND bep.consumerName = :consumerName " +
           "AND bep.status = 'PROCESSING'")
    Optional<BookingEventProcessed> findIfProcessing(@Param("eventId") String eventId,
                                                     @Param("consumerName") String consumerName);
    
    //Update status after processing
    @Modifying
    @Query("UPDATE BookingEventProcessed bep " +
           "SET bep.status = :status, " +
           "    bep.processedAt = :processedAt, " +
           "    bep.errorMessage = :errorMessage " +
           "WHERE bep.id = :id")
    void updateProcessingStatus(@Param("id") String id,
                               @Param("status") String status,
                               @Param("processedAt") Instant processedAt,
                               @Param("errorMessage") String errorMessage);
}
