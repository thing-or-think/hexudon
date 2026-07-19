package com.naprock.hexudon.application.dto.game;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request DTO used to select agent types for a team before the match starts.
 *
 * <p>Used by:
 * <ul>
 *     <li>POST /api/game/agent-types</li>
 * </ul>
 *
 * @param gameId Match identifier.
 * @param types  List of agent types corresponding to each agent
 *               (e.g. ["PATROL", "REFUEL"]).
 */
public record SelectAgentTypesRequest(

        @JsonProperty("game_id")
        @NotBlank(message = "game_id must not be blank")
        String gameId,

        @NotEmpty(message = "types must not be empty")
        List<@NotBlank(message = "agent type must not be blank") String> types

) {
}