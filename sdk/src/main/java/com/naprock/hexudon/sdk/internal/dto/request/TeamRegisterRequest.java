package com.naprock.hexudon.sdk.internal.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;


/**
 * DTO request used for registering a team into Hexudon game server.
 *
 * <p>
 * This DTO is only used internally for JSON serialization.
 * It is mapped from public domain model
 * {@code TeamRegistration} through {@code TeamRegisterMapper}.
 * </p>
 *
 * <p>
 * JSON representation:
 * </p>
 *
 * <pre>
 * {
 *   "teamName": "my-team",
 *   "types": [0, 1]
 * }
 * </pre>
 *
 * <p>
 * Agent type values:
 * </p>
 *
 * <ul>
 *     <li>0 - PATROL</li>
 *     <li>1 - REFUEL</li>
 * </ul>
 *
 * @param teamId team identifier mapped to JSON field {@code teamName}
 * @param types agent type list
 */
public record TeamRegisterRequest(

        @JsonProperty("teamName")
        String teamId,

        List<Integer> types

) {


    /**
     * Compact constructor.
     *
     * <p>
     * Validates request data and guarantees immutability.
     * </p>
     *
     * @throws NullPointerException
     *         when teamId or types is null
     *
     * @throws IllegalArgumentException
     *         when agent type is invalid
     */
    public TeamRegisterRequest {


        Objects.requireNonNull(
                teamId,
                "teamId must not be null"
        );


        Objects.requireNonNull(
                types,
                "types must not be null"
        );


        types.forEach(
                type -> {

                    if (type == null) {

                        throw new IllegalArgumentException(
                                "types must not contain null element"
                        );
                    }


                    if (type != 0 && type != 1) {

                        throw new IllegalArgumentException(
                                "Unsupported agent type: " + type
                        );
                    }
                }
        );


        types =
                List.copyOf(types);
    }
}