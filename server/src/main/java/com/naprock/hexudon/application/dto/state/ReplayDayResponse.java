package com.naprock.hexudon.application.dto.state;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Replay information for one game day.
 *
 * @param day game day
 * @param steps total steps in the day
 * @param roadCondition road traffic condition by cell
 * @param teams replay information of all teams
 */
public record ReplayDayResponse(

        int day,

        int steps,

        @JsonProperty("road_condition")
        Map<Integer, Integer> roadCondition,

        List<ReplayTeamResponse> teams

) {
}