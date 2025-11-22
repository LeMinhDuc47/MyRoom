        package com.myroom.bookingservice.client;

import com.myroom.bookingservice.client.dto.PaymentResponse;
import feign.FeignException;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class PaymentServiceClientFallbackFactory implements FallbackFactory<PaymentServiceClient> {
    @Override
    public PaymentServiceClient create(Throwable cause) {
        return new PaymentServiceClient() {
            @Override
            public PaymentResponse getPayment(String id) {
                String reason;
                if (cause instanceof FeignException) {
                    reason = "FeignException status=" + ((FeignException) cause).status();
                } else {
                    reason = cause.getClass().getSimpleName();
                }
                return PaymentResponse.builder()
                        .id(id)
                        .status("UNAVAILABLE")
                        .message("Payment Service fallback invoked: " + reason)
                        .build();
            }
        };
    }
}
