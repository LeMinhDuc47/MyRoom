package com.myroom.bookingservice.outbox;

import com.myroom.bookingservice.usecase.KafkaMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class OutboxRelayService {
    private final OutboxEventRepository outboxEventRepository;
    private final KafkaMessageService kafkaMessageService;
    private final OutboxService outboxService;

    @Value("${outbox.kafka.topic:booking.mail}")
    private String kafkaTopic;

    public OutboxRelayService(
            OutboxEventRepository outboxEventRepository,
            KafkaMessageService kafkaMessageService,
            OutboxService outboxService) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaMessageService = kafkaMessageService;
        this.outboxService = outboxService;
    }

    @Scheduled(fixedDelayString = "${outbox.relay.interval:10000}")
    @Transactional
    public void relayOutboxEvents() {
        try {
            List<OutboxEvent> events = outboxEventRepository.findByProcessedFalseOrderByCreatedAtAsc();

            if (events.isEmpty()) {
                log.debug("No unprocessed outbox events to relay");
                return;
            }

            log.info("Found {} unprocessed outbox events to relay", events.size());

            for (OutboxEvent event : events) {
                try {
                    log.info("Relaying outbox event: {} (type: {}, aggregate: {})", 
                            event.getId(), event.getType(), event.getAggregateId());

                    // Gửi message đến Kafka
                    kafkaMessageService.sendMessage(kafkaTopic, event.getPayload());

                    // Đánh dấu event đã được xử lý
                    outboxService.markAsProcessed(event.getId());

                    log.info("Successfully relayed outbox event: {}", event.getId());

                } catch (Exception e) {
                    log.error("Failed to relay outbox event {}: {}", event.getId(), e.getMessage(), e);
                    // Event sẽ được retry ở lần chạy tiếp theo
                }
            }
        } catch (Exception e) {
            log.error("Error in outbox relay service: {}", e.getMessage(), e);
        }
    }
}
