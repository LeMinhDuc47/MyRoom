package com.myroom.onboardingservice.usecase.impl;

import com.myroom.onboardingservice.api.constants.ApiConstants;
import com.myroom.onboardingservice.data.dto.AccountOnboardingResponseDto;
import com.myroom.onboardingservice.data.dto.OrganizationAccountOnboardingRequestModelDto;
import com.myroom.onboardingservice.data.dto.OrganizationResponseDto;
import com.myroom.onboardingservice.data.dto.StripeAccountLinkResponseDto;
import com.myroom.onboardingservice.exception.OnboardingServiceRuntimeException;
import com.myroom.onboardingservice.exception.OrganizationServiceException;
import com.myroom.onboardingservice.usecase.OrganizationPayService;
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
public class OrganizationPayServiceImpl implements OrganizationPayService {
    @Autowired
    WebClient webClient;

    @Autowired
    EurekaClientUtil eurekaClientUtil;

    @Value("${services.organization-pay.id}")
    String organizationPayServiceID;

    @Value("${services.organization-pay.v1.api}")
    String organizationPayServiceV1;

    @Value("${services.organization-pay.v1.name}")
    String organizationPayServiceV1Name;

    @Override
    public AccountOnboardingResponseDto onboardOrganizationAccount(String organizationId,
            OrganizationAccountOnboardingRequestModelDto organizationAccountOnboardingRequestModelDto) {
        log.info("Calling {} Service for onboarding organization account", organizationPayServiceV1Name);

        try {
            // Construct URL
            String organizationPayServiceURI = eurekaClientUtil.getServiceUri(organizationPayServiceID);
            String url = organizationPayServiceURI + organizationPayServiceV1 + "/onboarding/" + organizationId;

            AccountOnboardingResponseDto responseDto = webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(organizationAccountOnboardingRequestModelDto)
                    .retrieve()
                    .bodyToMono(AccountOnboardingResponseDto.class)
                    .block();
            log.info("Created organization account: {}", responseDto);
            return responseDto;
        } catch (WebClientResponseException webClientResponseException) {
            handleWebClientException(webClientResponseException);
            // This will never be reached as handleWebClientException always throws an
            // exception
            return null;
        } catch (Exception ex) {
            log.error("Error onboarding organization account: {}", ex.getMessage(), ex);
            throw new OnboardingServiceRuntimeException("Failed to onboard organization account: " + ex.getMessage());
        }
    }

    private void handleWebClientException(WebClientResponseException webClientResponseException) {
        log.error("WebClientException from {} Service - Status: {}, Response: {}",
                organizationPayServiceV1Name,
                webClientResponseException.getStatusCode(),
                webClientResponseException.getResponseBodyAsString());

        String responseBody = webClientResponseException.getResponseBodyAsString();

        try {
            // Try to parse error response as JSONObject
            JSONObject response = new JSONObject(responseBody);
            log.info("Error response from {} Service is: {}", organizationPayServiceV1Name, response);

            String errorCode = response.has("errorCode") ? response.getString("errorCode")
                    : ApiConstants.INTERNAL_SERVER_ERROR;
            String message = response.has("message") ? response.getString("message")
                    : "Error from organization pay service";
            String details = response.has("details") ? response.getString("details") : responseBody;

            throw new OrganizationServiceException(errorCode, message, details);
        } catch (org.json.JSONException jsonException) {
            // If response is not valid JSON, handle it as a generic error
            log.error("Failed to parse error response as JSON: {}", responseBody);
            String errorMessage = "Error from organization pay service: " + webClientResponseException.getStatusCode();
            if (responseBody != null && !responseBody.isEmpty()) {
                errorMessage += " - " + responseBody;
            }
            throw new OrganizationServiceException(
                    ApiConstants.INTERNAL_SERVER_ERROR,
                    errorMessage,
                    "Unable to parse error response from organization pay service");
        }
    }
}