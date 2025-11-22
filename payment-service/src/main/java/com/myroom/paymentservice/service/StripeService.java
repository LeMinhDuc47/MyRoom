package com.myroom.paymentservice.service;

import com.myroom.paymentservice.exception.PaymentProcessingException;
import com.myroom.paymentservice.service.dto.PaymentIntentResult;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class StripeService {

    @Value("${stripe.api.secret-key}")
    private String stripeSecretKey;

    @CircuitBreaker(name = "stripeCircuitBreaker", fallbackMethod = "handleStripeFailure")
    public PaymentIntentResult createPaymentIntent(Long amount, String currency, Map<String, String> metadata) throws StripeException {
        Stripe.apiKey = stripeSecretKey;

        PaymentIntentCreateParams.Builder builder = PaymentIntentCreateParams.builder()
                .setAmount(amount)
                .setCurrency(currency);
        if (metadata != null && !metadata.isEmpty()) {
            metadata.forEach(builder::putMetadata);
        }

        PaymentIntentCreateParams params = builder.build();
        PaymentIntent intent = PaymentIntent.create(params);
        return PaymentIntentResult.builder()
                .success(true)
                .clientSecret(intent.getClientSecret())
                .message("Payment intent created")
                .build();
    }

    // Fallback now always throws PaymentProcessingException to enforce fail-fast behavior
    public PaymentIntentResult handleStripeFailure(Long amount, String currency, Map<String, String> metadata, Throwable t) {
        String baseMessage = (t instanceof CallNotPermittedException)
                ? "Payments temporarily unavailable due to circuit breaker being OPEN. Please try again later."
                : "Unable to process payment intent at the moment. Please try again later.";
        throw new PaymentProcessingException(baseMessage, t);
    }
}
