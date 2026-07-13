package com.naprock.hexudon.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.Objects;

/**
 * Request DTO containing all agent actions submitted by a team
 * for the current turn.
 *
 * <p>This DTO contains a flat list of actions performed by the team's
 * agents. It is received by the action submission API and converted
 * into domain objects by the application mapper.</p>
 *
 * @param actions the list of agent actions to execute
 */
public record SubmitActionRequest(

        @NotEmpty(message = "Action list must not be empty")
        List<@Valid AgentActionDto> actions

) {

    /**
     * Creates an immutable action submission request.
     *
     * <p>This constructor validates that the action list is not
     * {@code null} or empty and creates a defensive copy to prevent
     * external modification.</p>
     *
     * @throws IllegalArgumentException if {@code actions} is empty
     * @throws NullPointerException if {@code actions} is {@code null}
     */
    public SubmitActionRequest {
        Objects.requireNonNull(actions, "actions must not be null");

        if (actions.isEmpty()) {
            throw new IllegalArgumentException("actions must not be empty");
        }

        actions = List.copyOf(actions);
    }
}