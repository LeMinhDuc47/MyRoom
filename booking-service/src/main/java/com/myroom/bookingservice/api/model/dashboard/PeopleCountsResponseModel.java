package com.myroom.bookingservice.api.model.dashboard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class PeopleCountsResponseModel {
    @Schema(description = "single", example = "12")
    private Long singleCounts;

    @Schema(description = "double", example = "120")
    private Long doubleCounts;

    @Schema(description = "others", example = "130")
    private Long othersCounts;
}
