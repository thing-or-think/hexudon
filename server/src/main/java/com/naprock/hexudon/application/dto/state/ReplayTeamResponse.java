package com.naprock.hexudon.application.dto.state;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Replay information for one team.
 *
 * @param teamId team identifier
 * @param kinds agent kinds
 * @param servings total servings
 * @param types distinct udon types
 * @param submitted whether the team submitted actions
 * @param frames replay frames
 */
public record ReplayTeamResponse(

        @JsonProperty("team_id")
        String teamId,

        List<Integer> kinds,

        int servings,

        int types,

        boolean submitted,

        List<ReplayFrameResponse> frames

) {
}