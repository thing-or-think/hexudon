package com.naprock.hexudon.application.dto;

import com.naprock.hexudon.domain.valueobject.ActionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

/**
 * DTO representing a single action submitted for an agent.
 *
 * <p>Each action specifies the agent that performs it, the execution
 * order within the turn, the action type, and an optional destination
 * coordinate. The destination coordinate is required only for
 * {@link ActionType#MOVE} actions.</p>
 *
 * @param agentId the unique identifier of the agent
 * @param order the execution order of the action within the turn
 * @param actionType the type of action to perform
 * @param coordinate the destination coordinate for a move action, or {@code null} for a wait action
 */
public record AgentActionDto(

        @NotBlank(message = "Agent ID must not be blank")
        String agentId,

        @Min(value = 1, message = "Order must be greater than or equal to 1")
        int order,

        @NotNull(message = "Action type must not be null")
        ActionType actionType,

        @Valid
        CoordinateResponse coordinate

) {

    /**
     * Creates an immutable agent action DTO.
     *
     * <p>This constructor validates the basic constraints of the request.
     * Validation of whether a coordinate is required for a specific
     * {@link ActionType} is handled by the application layer or mapper.</p>
     *
     * @throws IllegalArgumentException if {@code agentId} is blank or {@code order} is less than {@code 1}
     * @throws NullPointerException if {@code actionType} is {@code null}
     */
    public AgentActionDto {
        if (agentId == null || agentId.isBlank()) {
            throw new IllegalArgumentException("agentId must not be blank");
        }

        if (order < 1) {
            throw new IllegalArgumentException("order must be greater than or equal to 1");
        }

        Objects.requireNonNull(actionType, "actionType must not be null");
    }
}