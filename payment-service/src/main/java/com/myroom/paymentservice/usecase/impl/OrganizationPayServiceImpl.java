package com.myroom.paymentservice.usecase.impl;

import com.myroom.paymentservice.api.constants.ApiConstants;
import com.myroom.paymentservice.data.dto.OrganizationAccountDetails;
import com.myroom.paymentservice.exception.OrganizationAccountNotActiveException;
import com.myroom.paymentservice.exception.OrganizationAccountNotFoundException;
import com.myroom.paymentservice.exception.OrganizationPayServiceException;
import com.myroom.paymentservice.exception.PaymentServiceRuntimeException;
import com.myroom.paymentservice.usecase.OrganizationPayService;
import com.myroom.paymentservice.util.EurekaClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    public boolean isOrganizationAccountActive(String organizationId) {
        log.info("Calling {} Service for verifying organization account is active or not for organizationId: {}",
                organizationPayServiceV1Name, organizationId);

        // Construct URL
        String organizationPayServiceURI = eurekaClientUtil.getServiceUri(organizationPayServiceID);
        String url = organizationPayServiceURI + organizationPayServiceV1 + "/account/" + organizationId + "/active";

        try {
            Boolean isActive = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();

            if (!isActive) {
                log.error("Organization account is not active");
                throw new OrganizationAccountNotActiveException(
                        ApiConstants.ORGANIZATION_ACCOUNT_NOT_ACTIVE,
                        "Payment processing is currently unavailable as the organization's account is inactive.",
                        "");
            }

            log.info("Organization account is active");
            return true;
        } catch (WebClientResponseException webClientResponseException) {
            handleWebClientException(webClientResponseException);
        } catch (Exception ex) {
            throw new PaymentServiceRuntimeException(ex.getMessage());
        }
        return false;
    }

    @Override
    public String getOrganizationStripeAccountId(String organizationId, String uid) {
        log.info("Calling {} Service for getting organization account Details for organizationId: {}",
                organizationPayServiceV1Name, organizationId);

        // Construct base URI
        String organizationPayServiceURI = eurekaClientUtil.getServiceUri(organizationPayServiceID);
        String accountUrl = organizationPayServiceURI + organizationPayServiceV1 + "/account/" + organizationId;
        String onboardingUrl = organizationPayServiceURI + organizationPayServiceV1 + "/onboarding/" + organizationId;

        try {
            OrganizationAccountDetails organizationAccountDetails = webClient.get()
                    .uri(accountUrl)
                    .retrieve()
                    .bodyToMono(OrganizationAccountDetails.class)
                    .block();

            log.info("Fetched Organization account details: {}", organizationAccountDetails);
            return organizationAccountDetails.getStripeAccountDetails().getAccountId();
        } catch (WebClientResponseException webClientResponseException) {
            // If organization account not found, attempt to create/onboard it and retry
            try {
                JSONObject response = new JSONObject(webClientResponseException.getResponseBodyAsString());
                String errorCode = response.optString("errorCode");
                if (ApiConstants.ORGANIZATION_ACCOUNT_NOT_FOUND.equals(errorCode)) {
                    log.info("Organization account not found for {} - requesting onboarding at {}", organizationId,
                            onboardingUrl);
                    // Call onboarding endpoint to create organization account (will return
                    // onboarding link)
                    // Build simple body with uid
                    var body = new java.util.HashMap<String, Object>();
                    body.put("uid", uid);

                    webClient.post()
                            .uri(onboardingUrl)
                            .bodyValue(body)
                            .retrieve()
                            .bodyToMono(Object.class)
                            .block();

                    // Retry fetching account details after onboarding request
                    OrganizationAccountDetails organizationAccountDetails = webClient.get()
                            .uri(accountUrl)
                            .retrieve()
                            .bodyToMono(OrganizationAccountDetails.class)
                            .block();

                    if (organizationAccountDetails != null
                            && organizationAccountDetails.getStripeAccountDetails() != null) {
                        return organizationAccountDetails.getStripeAccountDetails().getAccountId();
                    }
                }
            } catch (Exception retryEx) {
                log.error("Failed to auto-onboard or retrieve organization account: {}", retryEx.getMessage());
            }

            handleWebClientException(webClientResponseException);
        } catch (Exception ex) {
            throw new PaymentServiceRuntimeException(ex.getMessage());
        }
        return null;
    }

    private void handleWebClientException(WebClientResponseException webClientResponseException) {
        log.info("Handling WebClientException");

        // Convert error response to JSONObject
        JSONObject response = new JSONObject(webClientResponseException.getResponseBodyAsString());

        log.info("Error response from {} Service is: {}", organizationPayServiceV1Name, response);

        String errorCode = response.getString("errorCode");
        String message = response.getString("message");
        String details = response.getString("details");

        throw new OrganizationPayServiceException(errorCode, message, details);
    }
}