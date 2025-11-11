package com.myroom.bookingservice.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.myroom.bookingservice.api.constants.ContactDetails;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateContactDetailsRequestModel {
    @Schema(required = false)
    String bookingRequestId;

    @Schema(description = "contact details", required = true)
    ContactDetails details;

    @Schema(description = "user id", example = "901237412412", required = true)
    String uid;
}