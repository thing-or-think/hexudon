package com.naprock.hexudon.application.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO containing a game identifier.
 *
 * <p>Used by:
 * <ul>
 *     <li>POST /api/game/reset</li>
 *     <li>POST /api/game/practice/reset</li>
 * </ul>
 *
 * @param gameId unique identifier of the game
 */
public record GameRequest(

        /**
         * Unique identifier of the game.
         */
        @JsonProperty("game_id")
        @NotBlank(message = "game_id must not be blank")
        String gameId

) {
}