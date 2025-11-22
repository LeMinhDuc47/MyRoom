package com.myroom.bookingservice.client;

import com.myroom.bookingservice.client.dto.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "PAYMENT-SERVICE",
        path = "/api/v1/payment-service",
        contextId = "paymentServiceCB",
        fallbackFactory = PaymentServiceClientFallbackFactory.class
)
public interface PaymentServiceClient {

    @GetMapping("/payments/{id}")
    PaymentResponse getPayment(@PathVariable("id") String id);
}
