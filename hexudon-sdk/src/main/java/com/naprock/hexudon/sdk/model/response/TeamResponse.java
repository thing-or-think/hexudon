package com.naprock.hexudon.sdk.model.response;

import java.util.List;
import java.util.Objects;

/**
 * Response DTO containing the registered team information.
 *
 * @param id the assigned team identifier
 * @param agents the list of initial agent information
 */
public record TeamResponse(
        String id,
        List<AgentResponse> agents
) {

    /**
     * Creates a new {@code TeamResponse}.
     * <p>
     * If {@code agents} is {@code null}, an empty immutable list is used.
     *
     * @throws NullPointerException if {@code id} is {@code null}
     * @throws IllegalArgumentException if {@code id} is blank
     */
    public TeamResponse {
        Objects.requireNonNull(id, "id must not be null");

        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }

        agents = agents == null
                ? List.of()
                : List.copyOf(agents);
    }
}