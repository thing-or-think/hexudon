package com.naprock.hexudon.application.dto.game;

import com.naprock.hexudon.application.dto.agent.AgentResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Response DTO representing a visible opponent team.
 *
 * @param id     Opponent team identifier.
 * @param agents Visible agents of the opponent.
 */
public record OpponentResponse(

        @Min(value = 0, message = "id must be greater than or equal to 0")
        int id,

        @NotNull
        @Valid
        List<AgentResponse> agents

) {
}