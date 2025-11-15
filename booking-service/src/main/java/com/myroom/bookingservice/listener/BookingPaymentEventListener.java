package com.myroom.bookingservice.listener;

import com.myroom.bookingservice.usecase.BookingService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BookingPaymentEventListener {
    @Autowired
    BookingService bookingService;

    @RetryableTopic(attempts = "4", backoff = @Backoff(delay = 2000, multiplier = 2.0), autoCreateTopics = "true", dltTopicSuffix = "-dlt", exclude = {
            JSONException.class })
    @KafkaListener(topics = "booking.payment")
    public void listen(final String payload) {
        log.info("Received booking payment event (kafka topic: 'booking.payment') with payload:{}", payload);
        try {
            JSONObject jsonPayload = new JSONObject(payload);
            String bookingId = jsonPayload.getString("bookingId");
            String status = jsonPayload.getString("status");
            if ("complete".equals(status)) {
                bookingService.updateBookingStatusAndBookingPaymentMetaDataModel(bookingId, status);
            } else {
                // treat any non-complete payment status as cancel/compensation
                bookingService.handleBookingPaymentCancel(bookingId);
            }
        } catch (JSONException jsonException) {
            log.error("Invalid message received: {}", jsonException.getMessage());
            throw jsonException;
        } catch (Exception ex) {
            log.error("Some error occurred: {}", ex.getMessage());
            throw ex;
        }
    }
}