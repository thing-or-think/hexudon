package com.naprock.hexudon.sdk.internal.dto.request;

import java.util.List;
import java.util.Objects;


/**
 * DTO request used to submit agent actions in practice mode.
 *
 * <p>
 * This DTO represents the JSON payload sent to the practice
 * action endpoint.
 * </p>
 *
 * <pre>
 * {
 *   "gameId": "practice-001",
 *   "day": 1,
 *   "actions": [
 *     [0, 1],
 *     [3, 4]
 *   ]
 * }
 * </pre>
 *
 * <p>
 * This class is immutable and only used internally for transport
 * serialization.
 * </p>
 *
 * @param gameId practice game identifier
 * @param day current practice day
 * @param actions agent movement actions
 */
public record PracticeSubmitRequest(

        String gameId,

        int day,

        List<List<Integer>> actions

) {


    /**
     * Compact constructor.
     *
     * <p>
     * Validates request fields and creates deep immutable copies
     * of nested action lists.
     * </p>
     *
     * @throws IllegalArgumentException
     *         when gameId is blank or day is negative
     *
     * @throws NullPointerException
     *         when actions is null
     */
    public PracticeSubmitRequest {

        if (gameId == null || gameId.isBlank()) {

            throw new IllegalArgumentException(
                    "gameId must not be blank"
            );
        }


        if (day < 0) {

            throw new IllegalArgumentException(
                    "day must not be negative"
            );
        }


        Objects.requireNonNull(
                actions,
                "actions must not be null"
        );


        actions =
                actions.stream()
                        .map(
                                List::copyOf
                        )
                        .toList();


        actions =
                List.copyOf(actions);
    }
}