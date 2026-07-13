package com.naprock.hexudon.application.dto;

import com.naprock.hexudon.domain.valueobject.AgentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

/**
 * Request DTO used to register an agent for a team.
 *
 * <p>This DTO contains the unique agent identifier and the type of agent
 * to be registered. It is received by the agent registration API and
 * validated automatically by Spring Boot using Jakarta Bean Validation.</p>
 *
 * @param agentId the unique identifier of the agent
 * @param agentType the type of agent to register
 */
public record AgentRegisterRequest(

        @NotBlank(message = "Agent ID must not be blank")
        String agentId,

        @NotNull(message = "Agent type must not be null")
        AgentType agentType
) {

    /**
     * Creates an immutable agent registration request.
     *
     * <p>This constructor validates that the agent identifier is not blank
     * and the agent type is not {@code null}.</p>
     *
     * @throws IllegalArgumentException if {@code agentId} is blank
     * @throws NullPointerException if {@code agentType} is {@code null}
     */
    public AgentRegisterRequest {
        if (agentId == null || agentId.isBlank()) {
            throw new IllegalArgumentException("agentId must not be blank");
        }

        Objects.requireNonNull(agentType, "agentType must not be null");
    }
}