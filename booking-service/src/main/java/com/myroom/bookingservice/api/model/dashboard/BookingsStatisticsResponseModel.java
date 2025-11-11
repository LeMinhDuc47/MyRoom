package com.myroom.bookingservice.api.model.dashboard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingsStatisticsResponseModel {
    @Schema(description = "totalBookings", example = "1200")
    private Long totalBookings;

    @Schema(description = "total", example = "120")
    private Long total;

    @Schema(description = "data", example = "[13, 18, 7, 30, 19, 34, 1]")
    private List<Long> data;
}
