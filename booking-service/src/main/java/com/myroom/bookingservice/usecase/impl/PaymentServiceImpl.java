package com.myroom.bookingservice.usecase.impl;

import com.myroom.bookingservice.client.PaymentServiceClient;
import com.myroom.bookingservice.data.dto.OnlinePaymentOrderResponseDto;
import com.myroom.bookingservice.data.dto.PaymentOrderRequestDto;
import com.myroom.bookingservice.data.dto.PaymentOrderResponseDto;
import com.myroom.bookingservice.usecase.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentServiceClient paymentServiceClient;

    @Override
    public PaymentOrderResponseDto createPaymentOrder(PaymentOrderRequestDto paymentOrderRequestDto) {
        log.info("Calling PAYMENT-SERVICE for creating payment order: {}", paymentOrderRequestDto);
        OnlinePaymentOrderResponseDto response = paymentServiceClient.createPaymentOrder(paymentOrderRequestDto);
        log.info("Payment service responded with: {}", response);

        if (response == null) {
            log.warn("Payment service returned null response, defaulting to pending status.");
            return PaymentOrderResponseDto.builder()
                    .id("FALLBACK_" + System.currentTimeMillis())
                    .amount(paymentOrderRequestDto != null ? paymentOrderRequestDto.getAmount() : "0")
                    .status("PAYMENT_PENDING")
                    .url("http://localhost:3000/payment-pending")
                    .build();
        }

        return PaymentOrderResponseDto.builder()
                .id(response.getId())
                .amount(response.getAmount())
                .status(response.getStatus())
                .url(response.getUrl())
                .build();
    }
}