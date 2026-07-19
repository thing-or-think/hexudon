package com.naprock.hexudon.application.dto.state;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Response DTO representing the current game state.
 *
 * <p>Used by:
 * <ul>
 *     <li>GET /api/game/state</li>
 * </ul>
 *
 * @param status current game status (waiting, in_progress, finished)
 * @param day current game day
 * @param stepsToday number of steps used today
 * @param dayDeadlineIn remaining seconds until the current day ends
 * @param roadCondition road traffic conditions indexed by cell
 * @param teams current state of all teams indexed by team id
 */
public record GameStateResponse(

        String status,

        int day,

        @JsonProperty("steps_today")
        int stepsToday,

        @JsonProperty("day_deadline_in")
        double dayDeadlineIn,

        @JsonProperty("road_condition")
        Map<Integer, Integer> roadCondition,

        Map<String, TeamStateResponse> teams

) {
}