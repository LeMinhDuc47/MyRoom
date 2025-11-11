package com.myroom.bookingservice.api.constants;

import com.fasterxml.jackson.annotation.JsonInclude;
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
public class ContactDetails {
    @Schema(description = "full Name", example = "Pawan Kumar", required = true)
    String fullName;

    @Schema(description = "email", example = "name@gmail.com", required = true)
    String emailId;

    @Schema(description = "phone no", example = "+91 1234567890", required = true)
    String phoneNumber;
}
