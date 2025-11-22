package com.myroom.bookingservice.usecase.impl;

import com.myroom.bookingservice.data.dto.PaymentOrderRequestDto;
import com.myroom.bookingservice.data.dto.PaymentOrderResponseDto;
import com.myroom.bookingservice.exception.BookingServiceRuntimeException;
import com.myroom.bookingservice.exception.PaymentServiceException;
import com.myroom.bookingservice.exception.PaymentServiceUnavailableException;
import com.myroom.bookingservice.api.constants.ApiConstants;
import com.myroom.bookingservice.usecase.PaymentService;
import com.myroom.bookingservice.utils.EurekaClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    EurekaClientUtil eurekaClientUtil;

    @Autowired
    WebClient webClient;

    @Value("${services.payment-service.id}")
    String paymentServiceID;

    @Value("${services.payment-service.v1.api}")
    String paymentServiceV1;

    @Value("${services.payment-service.v1.name}")
    String paymentServiceV1Name;

    @Override
    @CircuitBreaker(name = "paymentService", fallbackMethod = "createPaymentOrderFallback")
    public PaymentOrderResponseDto createPaymentOrder(PaymentOrderRequestDto paymentOrderRequestDto) {
        log.info("Calling {} Service for creating payment order: {}", paymentServiceV1Name, paymentOrderRequestDto);

        PaymentOrderResponseDto paymentOrder = null;

        // Construct URL
        String paymentServiceUri = eurekaClientUtil.getServiceUri(paymentServiceID);
        String url = paymentServiceUri + paymentServiceV1 + "/orders";

        try {
            PaymentOrderResponseDto paymentOrderResponse = webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(paymentOrderRequestDto)
                .retrieve()
                .bodyToMono(PaymentOrderResponseDto.class)
                .block();

            log.info("Payment Order created successfully: {}", paymentOrderResponse);
            paymentOrder = paymentOrderResponse;
        } catch (WebClientResponseException webClientResponseException) {
            handleWebClientException(webClientResponseException);
        }

        return paymentOrder;
    }

    private void handleWebClientException(WebClientResponseException webClientResponseException) {
        log.info("Handling WebClientException");

        // Convert error response to JSONObject
        JSONObject response = new JSONObject(webClientResponseException.getResponseBodyAsString());

        log.info("Error response from {} Service is: {}", paymentServiceV1Name, response);

        String errorCode = response.getString("errorCode");
        String message = response.getString("message");
        String details = response.getString("details");

        throw new PaymentServiceException(errorCode, message, details);
    }

    private PaymentOrderResponseDto createPaymentOrderFallback(PaymentOrderRequestDto paymentOrderRequestDto, Throwable throwable) {
        // Fallback always throws service unavailable to propagate 503 upstream
        log.warn("Fallback triggered for paymentService. Reason: {}", throwable.getMessage());
        throw new PaymentServiceUnavailableException(
                ApiConstants.PAYMENT_SERVICE_UNAVAILABLE,
                ApiConstants.MESSAGE_SERVICE_UNAVAILABLE,
                throwable.getMessage()
        );
    }
}