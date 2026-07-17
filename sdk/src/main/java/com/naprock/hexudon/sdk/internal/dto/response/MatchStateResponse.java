package com.naprock.hexudon.sdk.internal.dto.response;

import com.naprock.hexudon.sdk.model.MatchStatus;

import java.util.List;

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
 * @param agents Current player's Agents
 * @param others Opponent Agents
 * @param traffics Recorded traffic information
 * @param status Current match status
 */
public record MatchStateResponse(
        long endsAt,
        int day,
        List<AgentResponse> agents,
        List<AgentResponse> others,
        List<TrafficResponse> traffics,
        MatchStatus status
) {

    /**
     * Compact constructor validating required fields
     * and creating immutable collection copies.
     */
    public MatchStateResponse {
        if (agents == null) {
            throw new IllegalArgumentException(
                    "Agents must not be null"
            );
        }

        if (others == null) {
            throw new IllegalArgumentException(
                    "Others must not be null"
            );
        }

        if (traffics == null) {
            throw new IllegalArgumentException(
                    "Traffics must not be null"
            );
        }

        if (status == null) {
            throw new IllegalArgumentException(
                    "Match status must not be null"
            );
        }

        agents = List.copyOf(agents);
        others = List.copyOf(others);
        traffics = List.copyOf(traffics);
    }
}