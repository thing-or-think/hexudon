package com.naprock.hexudon.application.dto.state;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Response DTO representing a team's current state.
 *
 * @param typesSelected whether the team has selected udon types
 * @param agents current agents
 * @param stock current ingredient stock by cell
 * @param totalServings total servings delivered
 * @param distinctTypes collected distinct udon types
 * @param submitCount number of submissions made today
 * @param lastSubmittedAt timestamp of the latest submission, null if none
 */
public record TeamStateResponse(

        @JsonProperty("types_selected")
        boolean typesSelected,

        List<AgentStateResponse> agents,

        Map<Integer, Integer> stock,

        @JsonProperty("total_servings")
        int totalServings,

        @JsonProperty("distinct_types")
        List<Integer> distinctTypes,

        @JsonProperty("submit_count")
        int submitCount,

        @JsonProperty("last_submitted_at")
        Double lastSubmittedAt

) {
}