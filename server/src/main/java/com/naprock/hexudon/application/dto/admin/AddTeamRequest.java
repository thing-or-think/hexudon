package com.naprock.hexudon.application.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request DTO used to add a new team to an existing game.
 *
 * <p>Used by:
 * <ul>
 *     <li>POST /api/game/teams</li>
 * </ul>
 *
 * @param gameId unique identifier of the game
 * @param teamId unique identifier of the team
 * @param agents selected agent types of the team
 */
public record AddTeamRequest(

        /**
         * Unique identifier of the game.
         */
        @JsonProperty("game_id")
        @NotBlank(message = "game_id must not be blank")
        String gameId,

        /**
         * Unique identifier of the team.
         */
        @JsonProperty("team_id")
        @NotBlank(message = "team_id must not be blank")
        String teamId,

        /**
         * Selected agent types.
         */
        @NotNull(message = "agents must not be null")
        @NotEmpty(message = "agents must not be empty")
        List<Integer> agents

) {
}