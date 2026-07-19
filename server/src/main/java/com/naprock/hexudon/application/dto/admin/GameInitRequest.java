package com.naprock.hexudon.application.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.naprock.hexudon.application.dto.board.MapResponse;
import com.naprock.hexudon.application.dto.board.SpotResponse;
import com.naprock.hexudon.application.dto.team.TeamResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request DTO used to initialize a new game.
 *
 * <p>Used by:
 * <ul>
 *     <li>POST /api/game/init</li>
 * </ul>
 *
 * @param gameId unique identifier of the game
 * @param startsAt match start timestamp (Unix epoch seconds)
 * @param daySeconds duration of each game day
 * @param daySteps maximum allowed steps for each day
 * @param map map configuration
 * @param spots list of Udon spot configurations
 * @param fuelLimits maximum fuel capacity
 * @param players number of players
 * @param busyThreshold traffic busy threshold
 * @param jammedThreshold traffic jammed threshold
 * @param teams participating teams
 * @param agentSelectionTimeLimit time limit for selecting agent types
 * @param isPractice whether this is a practice match
 */
public record GameInitRequest(

        @JsonProperty("game_id")
        @NotBlank(message = "game_id must not be blank")
        String gameId,

        @NotNull(message = "startsAt must not be null")
        Long startsAt,

        @NotNull(message = "daySeconds must not be null")
        @NotEmpty(message = "daySeconds must not be empty")
        List<Double> daySeconds,

        @NotNull(message = "daySteps must not be null")
        @NotEmpty(message = "daySteps must not be empty")
        List<Integer> daySteps,

        @NotNull(message = "map must not be null")
        @Valid
        MapResponse map,

        @NotNull(message = "spots must not be null")
        @Valid
        List<SpotResponse> spots,

        @Min(value = 1, message = "fuelLimits must be greater than 0")
        int fuelLimits,

        @Min(value = 1, message = "players must be greater than 0")
        int players,

        @DecimalMin(value = "0.0", message = "busyThreshold must be non-negative")
        double busyThreshold,

        @DecimalMin(value = "0.0", message = "jammedThreshold must be non-negative")
        double jammedThreshold,

        @NotNull(message = "teams must not be null")
        @Valid
        List<TeamResponse> teams,

        @JsonProperty("agent_selection_time_limit")
        @DecimalMin(value = "0.0", message = "agentSelectionTimeLimit must be non-negative")
        double agentSelectionTimeLimit,

        @JsonProperty("is_practice")
        boolean isPractice

) {
}