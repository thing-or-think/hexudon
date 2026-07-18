package com.naprock.hexudon.sdk.internal.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

/**
 * DTO request used for registering a team and assigning agent roles.
 *
 * <p>Visibility: package-private.</p>
 *
 * @param gameId Unique identifier of the game
 * @param types Agent type codes list (0 = PATROL, 1 = REFUEL)
 */
public record TeamRegisterRequest(
        @JsonProperty("game_id") String gameId,
        @JsonProperty("types") List<Integer> types
) {

    /**
     * Compact constructor validating registration request.
     */
    public TeamRegisterRequest {
        Objects.requireNonNull(gameId, "gameId must not be null");
        Objects.requireNonNull(types, "types must not be null");

        types.forEach(type -> {
            if (type == null) {
                throw new IllegalArgumentException("types must not contain null element");
            }
            if (type != 0 && type != 1) {
                throw new IllegalArgumentException("Unsupported agent type: " + type);
            }
        });

        types = List.copyOf(types);
    }
}
