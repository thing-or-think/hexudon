package com.naprock.hexudon.application.dto.state;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO containing the final score details of a team.
 *
 * @param distinctTypes Number of distinct udon types collected.
 * @param cumulativeDailyTypes Cumulative daily distinct udon types.
 * @param totalServings Total number of servings delivered.
 * @param cumulativeResponseTime Total response time.
 */
public record TeamResultResponse(

        @JsonProperty("distinct_types")
        int distinctTypes,

        @JsonProperty("cumulative_daily_types")
        int cumulativeDailyTypes,

        @JsonProperty("total_servings")
        int totalServings,

        @JsonProperty("cumulative_response_time")
        double cumulativeResponseTime

) {
}