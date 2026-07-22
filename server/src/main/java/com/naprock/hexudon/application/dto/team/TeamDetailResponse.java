package com.naprock.hexudon.application.dto.team;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.PositiveOrZero;

public record TeamDetailResponse(
        @JsonProperty("distinct_types")
        @PositiveOrZero
        int distinctTypes,

        @JsonProperty("cumulative_daily_types")
        @PositiveOrZero
        int cumulativeDailyTypes,

        @JsonProperty("total_servings")
        @PositiveOrZero
        int totalServings,

        @JsonProperty("cumulative_response_time")
        @PositiveOrZero
        double cumulativeResponseTime
) {}