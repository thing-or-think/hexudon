package com.naprock.hexudon.sdk.internal.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

/**
 * DTO response representing team details within the match state.
 *
 * <p>Visibility: package-private.</p>
 *
 * @param agents List of agents belonging to the team
 * @param distinctTypes List of distinct brands/types visited by the team
 */
public record TeamResponse(
        @JsonProperty("agents") List<AgentResponse> agents,
        @JsonProperty("distinct_types") List<String> distinctTypes
) {

    /**
     * Compact constructor validating response values.
     */
    public TeamResponse {
        Objects.requireNonNull(agents, "Agents list must not be null");
        Objects.requireNonNull(distinctTypes, "Distinct types list must not be null");

        agents = List.copyOf(agents);
        distinctTypes = List.copyOf(distinctTypes);
    }
}
