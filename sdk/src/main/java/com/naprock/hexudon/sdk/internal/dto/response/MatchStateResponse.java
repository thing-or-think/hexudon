package com.naprock.hexudon.sdk.internal.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Objects;

/**
 * Response DTO representing the dynamic state of a match.
 *
 * <p>This DTO is used for deserializing JSON responses received from
 * the Hexudon server.</p>
 *
 * <p>Visibility: package-private.</p>
 *
 * @param endsAt Unix timestamp representing current turn end time
 * @param day Current game day
 * @param stepsToday Steps allowed today
 * @param roadCondition Traffic status map (flat coordinate index -> congestion level)
 * @param teams Map containing teams details indexed by team ID
 * @param status Current match status as string
 */
public record MatchStateResponse(
        @JsonProperty("endsAt") long endsAt,
        @JsonProperty("day") int day,
        @JsonProperty("steps_today") int stepsToday,
        @JsonProperty("road_condition") Map<String, Integer> roadCondition,
        @JsonProperty("teams") Map<String, TeamResponse> teams,
        @JsonProperty("status") String status
) {

    /**
     * Compact constructor validating response values.
     */
    public MatchStateResponse {
        Objects.requireNonNull(roadCondition, "Road condition must not be null");
        Objects.requireNonNull(teams, "Teams must not be null");
        Objects.requireNonNull(status, "Status must not be null");

        roadCondition = Map.copyOf(roadCondition);
        teams = Map.copyOf(teams);
    }
}
