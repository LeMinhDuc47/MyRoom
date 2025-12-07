package com.myroom.bookingservice.client;

import com.myroom.bookingservice.data.dto.OnlinePaymentOrderResponseDto;
import com.myroom.bookingservice.data.dto.PaymentOrderRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentServiceClientFallbackFactory implements FallbackFactory<PaymentServiceClient> {
    @Override
    public PaymentServiceClient create(Throwable cause) {
        return new PaymentServiceClient() {
            @Override
            public OnlinePaymentOrderResponseDto createPaymentOrder(PaymentOrderRequestDto request) {
                String amount = request != null ? request.getAmount() : "0";
                log.warn("Falling back to dummy payment order due to: {}", cause != null ? cause.getMessage() : "unknown error");
                return OnlinePaymentOrderResponseDto.builder()
                        .id("FALLBACK_" + System.currentTimeMillis())
                        .status("PAYMENT_PENDING")
                        .url("http://localhost:3000/payment-pending")
                        .amount(amount)
                        .build();
            }
        };
    }
}
