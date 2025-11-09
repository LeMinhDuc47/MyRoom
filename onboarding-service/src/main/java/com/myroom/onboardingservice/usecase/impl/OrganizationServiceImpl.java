package com.myroom.onboardingservice.usecase.impl;

import com.myroom.onboardingservice.api.constants.ApiConstants;
import com.myroom.onboardingservice.data.dto.OrganizationRequestDto;
import com.myroom.onboardingservice.data.dto.OrganizationResponseDto;
import com.myroom.onboardingservice.exception.OnboardingServiceRuntimeException;
import com.myroom.onboardingservice.exception.OrganizationServiceException;
import com.myroom.onboardingservice.usecase.OrganizationService;
import com.myroom.onboardingservice.util.EurekaClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@Slf4j
public class OrganizationServiceImpl implements OrganizationService {
    @Autowired
    WebClient webClient;

    @Autowired
    EurekaClientUtil eurekaClientUtil;

    @Value("${services.organization-service.id}")
    String organizationServiceID;

    @Value("${services.organization-service.v1.api}")
    String organizationServiceV1;

    @Value("${services.organization-service.v1.name}")
    String organizationServiceV1Name;

    @Override
    public OrganizationResponseDto createOrganization(OrganizationRequestDto organizationRequestDto) {
        log.info("Calling {} Service for creating organization: {}", organizationServiceV1Name, organizationRequestDto);

        try {
            // Construct URL
            String organizationServiceURI = eurekaClientUtil.getServiceUri(organizationServiceID);
            String url = organizationServiceURI + organizationServiceV1 + "/org";

            OrganizationResponseDto organizationResponseDto = webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(organizationRequestDto)
                    .retrieve()
                    .bodyToMono(OrganizationResponseDto.class)
                    .block();

            log.info("Organization created successfully: {}", organizationResponseDto);
            return organizationResponseDto;
        } catch (WebClientResponseException webClientResponseException) {
            handleWebClientException(webClientResponseException);
            // This will never be reached as handleWebClientException always throws an
            // exception
            return null;
        } catch (Exception ex) {
            log.error("Error creating organization: {}", ex.getMessage(), ex);
            throw new OnboardingServiceRuntimeException("Failed to create organization: " + ex.getMessage());
        }
    }

    private void handleWebClientException(WebClientResponseException webClientResponseException) {
        log.error("WebClientException from {} Service - Status: {}, Response: {}",
                organizationServiceV1Name,
                webClientResponseException.getStatusCode(),
                webClientResponseException.getResponseBodyAsString());

        String responseBody = webClientResponseException.getResponseBodyAsString();

        try {
            // Try to parse error response as JSONObject
            JSONObject response = new JSONObject(responseBody);
            log.info("Error response from {} Service is: {}", organizationServiceV1Name, response);

            String errorCode = response.has("errorCode") ? response.getString("errorCode")
                    : ApiConstants.INTERNAL_SERVER_ERROR;
            String message = response.has("message") ? response.getString("message")
                    : "Error from organization service";
            String details = response.has("details") ? response.getString("details") : responseBody;

            throw new OrganizationServiceException(errorCode, message, details);
        } catch (org.json.JSONException jsonException) {
            // If response is not valid JSON, handle it as a generic error
            log.error("Failed to parse error response as JSON: {}", responseBody);
            String errorMessage = "Error from organization service: " + webClientResponseException.getStatusCode();
            if (responseBody != null && !responseBody.isEmpty()) {
                errorMessage += " - " + responseBody;
            }
            throw new OrganizationServiceException(
                    ApiConstants.INTERNAL_SERVER_ERROR,
                    errorMessage,
                    "Unable to parse error response from organization service");
        }
    }
}