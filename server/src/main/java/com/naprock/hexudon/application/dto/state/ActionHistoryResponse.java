package com.naprock.hexudon.application.dto.state;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response DTO representing one submitted action.
 *
 * @param day game day
 * @param teamId team identifier
 * @param plan action plan of each agent
 * @param submittedAt Unix timestamp when the plan was submitted
 * @param submitCount number of submissions for the day
 */
public record ActionHistoryResponse(

        int day,

        @JsonProperty("team_id")
        String teamId,

        List<List<Integer>> plan,

        @JsonProperty("submitted_at")
        double submittedAt,

        @JsonProperty("submit_count")
        int submitCount

) {
}