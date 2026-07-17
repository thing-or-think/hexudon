package com.naprock.hexudon.sdk.internal.dto.response;

import java.util.List;
import java.util.Objects;


/**
 * DTO response returned after successful team registration.
 *
 * <p>
 * This DTO represents the server response containing the assigned
 * team identifier and initial agent information.
 * </p>
 *
 * <pre>
 * {
 *   "id": "team-alpha",
 *   "agents": [
 *     {
 *       "id": 1,
 *       "type": 0
 *     }
 *   ]
 * }
 * </pre>
 *
 * <p>
 * This class is immutable and only used internally for JSON
 * deserialization before mapping into public domain models.
 * </p>
 *
 * @param id assigned team identifier
 * @param agents initial registered agents
 */
public record TeamResponse(

        String id,

        List<AgentResponse> agents

) {


    /**
     * Compact constructor.
     *
     * <p>
     * Validates response data and guarantees immutability.
     * </p>
     *
     * @throws IllegalArgumentException
     *         when id is blank
     *
     * @throws NullPointerException
     *         when agents is null
     */
    public TeamResponse {

        if (id == null || id.isBlank()) {

            throw new IllegalArgumentException(
                    "id must not be blank"
            );
        }


        Objects.requireNonNull(
                agents,
                "agents must not be null"
        );


        agents =
                List.copyOf(agents);
    }
}