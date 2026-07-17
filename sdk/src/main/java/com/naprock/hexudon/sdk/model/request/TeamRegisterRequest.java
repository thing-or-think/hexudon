package com.naprock.hexudon.sdk.model.request;

import java.util.List;

/**
 * Request DTO used to register a team agent types.
 *
 * <p>
 * Sent to API:
 * POST /api/game/agent-types
 *
 * @param gameId game identifier
 * @param types list of agent roles
 */
public record TeamRegisterRequest(
        String gameId,
        List<Integer> types
) {

    /**
     * Creates a team registration request.
     *
     * @throws IllegalArgumentException if input is invalid
     */
    public TeamRegisterRequest {

        if (gameId == null
                || gameId.isBlank()) {

            throw new IllegalArgumentException(
                    "gameId must not be blank"
            );
        }


        if (types == null) {

            throw new IllegalArgumentException(
                    "types must not be null"
            );
        }


        types = List.copyOf(types);


        for (Integer type : types) {

            if (type == null) {

                throw new IllegalArgumentException(
                        "agent type must not be null"
                );
            }


            if (type != 0 && type != 1) {

                throw new IllegalArgumentException(
                        "unsupported agent type: " + type
                );
            }
        }
    }
}