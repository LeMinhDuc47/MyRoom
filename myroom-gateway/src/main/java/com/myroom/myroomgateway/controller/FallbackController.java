package com.myroom.myroomgateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/booking")
    public ResponseEntity<Map<String, String>> bookingFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "service", "BOOKING-SERVICE",
                        "message", "Booking Service is temporarily unavailable. Please try again later."
                ));
    }

    @GetMapping("/payment")
    public ResponseEntity<Map<String, String>> paymentFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "service", "PAYMENT-SERVICE",
                        "message", "Payment Service is temporarily unavailable. Please try again later."
                ));
    }
}
