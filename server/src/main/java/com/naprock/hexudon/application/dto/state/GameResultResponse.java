package com.naprock.hexudon.application.dto.state;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Response DTO containing the final game ranking and score details.
 *
 * <p>Used by:
 * <ul>
 *     <li>GET /api/game/result</li>
 * </ul>
 *
 * @param ranking Ordered list of team IDs from highest to lowest rank.
 * @param detail Score details for each team, keyed by team ID.
 */
public record GameResultResponse(

        @JsonProperty("ranking")
        List<String> ranking,

        @JsonProperty("detail")
        Map<String, TeamResultResponse> detail

) {
}