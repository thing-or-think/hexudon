package com.naprock.hexudon.sdk.model.response;

import com.naprock.hexudon.sdk.model.MatchStatus;

import java.util.List;
import java.util.Objects;

/**
 * Response DTO containing the current state of a match.
 *
 * @param endsAt the Unix timestamp when the current turn ends
 * @param day the current match day
 * @param agents the current state of the player's agents
 * @param others the visible opponent agents
 * @param traffics the recorded traffic information
 * @param status the current match status
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
     * Creates a new {@code MatchStateResponse}.
     * <p>
     * Any {@code null} collection is replaced with an empty immutable list.
     *
     * @throws NullPointerException if {@code status} is {@code null}
     */
    public MatchStateResponse {
        Objects.requireNonNull(status, "status must not be null");

        agents = agents == null
                ? List.of()
                : List.copyOf(agents);

        others = others == null
                ? List.of()
                : List.copyOf(others);

        traffics = traffics == null
                ? List.of()
                : List.copyOf(traffics);
    }
}