package com.myroom.myroomgateway.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class RateLimiterConfig {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(resolveIp(exchange));
    }

    @Bean
    @Primary
    public KeyResolver userKeyResolver() {
        return exchange -> Mono.just(resolveUserId(exchange));
    }

    private String resolveIp(ServerWebExchange exchange) {
        InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
        if (remoteAddress == null) {
            return "unknown-ip";
        }
        return remoteAddress.getAddress().getHostAddress();
    }

    private String resolveUserId(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (!StringUtils.hasText(authHeader)) {
            return resolveIp(exchange); // fallback to IP if no token
        }
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return resolveIp(exchange); // fallback to IP if no bearer token
        }
        String token = authHeader.substring(7); // remove Bearer
        String[] parts = token.split("\\.");
        if (parts.length < 2) {
            return resolveIp(exchange);
        }
        try {
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            JsonNode node = objectMapper.readTree(payloadJson);
            // typical JWT subject claim
            if (node.hasNonNull("sub")) {
                return node.get("sub").asText();
            }
            // fallback alternative claim names
            if (node.hasNonNull("userId")) {
                return node.get("userId").asText();
            }
        } catch (Exception ignored) {
            return resolveIp(exchange);
        }
        return resolveIp(exchange);
    }
}
