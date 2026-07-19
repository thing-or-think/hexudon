package com.naprock.hexudon.application.dto.game;

import com.naprock.hexudon.application.dto.agent.AgentResponse;
import com.naprock.hexudon.application.dto.traffic.TrafficResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Response DTO containing the current game day state for a team.
 *
 * <p>Used by:
 * <ul>
 *     <li>GET /api/game/day</li>
 * </ul>
 *
 * @param endsAt   Remaining time (seconds) until the current day ends.
 * @param day      Current match day (0..N-1).
 * @param agents   List of the team's agents.
 * @param others   List of visible opponent teams and their agents.
 * @param traffics List of traffic information.
 */
public record GameDayResponse(

        @NotNull
        @Min(value = 0, message = "endsAt must be greater than or equal to 0")
        Double endsAt,

        @Min(value = 0, message = "day must be greater than or equal to 0")
        int day,

        @NotNull
        @Valid
        List<AgentResponse> agents,

        @NotNull
        @Valid
        List<OpponentResponse> others,

        @NotNull
        @Valid
        List<TrafficResponse> traffics

) {
}