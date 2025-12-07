package com.myroom.bookingservice.client;

import com.myroom.bookingservice.data.dto.OnlinePaymentOrderResponseDto;
import com.myroom.bookingservice.data.dto.PaymentOrderRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "PAYMENT-SERVICE",
        path = "/api/v1/payment-service",
        contextId = "paymentServiceCB",
        fallbackFactory = PaymentServiceClientFallbackFactory.class
)
public interface PaymentServiceClient {

    @PostMapping(value = "/orders")
    OnlinePaymentOrderResponseDto createPaymentOrder(@RequestBody PaymentOrderRequestDto request);
}
