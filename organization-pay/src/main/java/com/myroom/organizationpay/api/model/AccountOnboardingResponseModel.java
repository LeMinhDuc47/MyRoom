package com.myroom.organizationpay.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.myroom.organizationpay.api.constants.OrganizationAccountStatus;
import com.myroom.organizationpay.data.dto.CreateStripeAccountLinkResponseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountOnboardingResponseModel {
    private OrganizationAccountStatus status;

    private CreateStripeAccountLinkResponseDto link;
}